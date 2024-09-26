package com.example.spinlog.statistics.service;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.repository.dto.*;
import com.example.spinlog.statistics.service.dto.MBTIDailyAmountSumResponse;
import com.example.spinlog.statistics.service.dto.MBTIEmotionAmountAverageResponse;
import com.example.spinlog.statistics.service.dto.MBTISatisfactionAverageResponse;
import com.example.spinlog.statistics.service.dto.MBTIWordFrequencyResponse;
import com.example.spinlog.statistics.repository.MBTIStatisticsRepository;
import com.example.spinlog.statistics.loginService.AuthenticatedUserService;
import com.example.spinlog.statistics.service.wordanalysis.WordExtractionService;
import com.example.spinlog.user.entity.Mbti;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.example.spinlog.utils.StatisticsValues.PERIOD_CRITERIA;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MBTIStatisticsService {
    private final MBTIStatisticsRepository mbtiStatisticsRepository;
    private final WordExtractionService wordExtractionService;
    private final AuthenticatedUserService authenticatedUserService;

    public MBTIStatisticsService(MBTIStatisticsRepository mbtiStatisticsRepository, WordExtractionService wordExtractionService, AuthenticatedUserService authenticatedUserService) {
        this.mbtiStatisticsRepository = mbtiStatisticsRepository;
        this.wordExtractionService = wordExtractionService;
        this.authenticatedUserService = authenticatedUserService;
    }

    public MBTIEmotionAmountAverageResponse getAmountAveragesEachMBTIAndEmotionLast30Days(RegisterType registerType){
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(PERIOD_CRITERIA);
        List<MBTIEmotionAmountAverageDto> dtos = mbtiStatisticsRepository.getAmountAveragesEachMBTIAndEmotionBetweenStartDateAndEndDate(registerType, startDate, today);
        List<MBTIEmotionAmountAverageDto> dtosWithZeroPadding = addZeroAverageForMissingGenderEmotionPairs(dtos);

        return MBTIEmotionAmountAverageResponse.builder()
                .mbti(authenticatedUserService.getUserMBTI())
                .mbtiEmotionAmountAverages(
                        dtosWithZeroPadding.stream()
                                .collect(
                                        groupingBy(MBTIEmotionAmountAverageDto::getMbtiFactor))
                                .entrySet().stream()
                                .map((e) ->
                                        MBTIEmotionAmountAverageResponse.MBTIEmotionAmountAverage.of(e.getKey(), e.getValue()))
                                .sorted(Comparator.comparing(MBTIEmotionAmountAverageResponse.MBTIEmotionAmountAverage::getMbtiFactor))
                                .toList())
                .build();
    }

    private List<MBTIEmotionAmountAverageDto> addZeroAverageForMissingGenderEmotionPairs(List<MBTIEmotionAmountAverageDto> dtos) {
        Stream<MBTIEmotionAmountAverageDto> zeroStream = Arrays.stream(Emotion.values())
                .flatMap(e ->
                        Arrays.stream(MBTIFactor.values())
                                .map(f -> new MBTIEmotionAmountAverageDto(f, e, 0L)))
                .filter(zeroDto -> dtos.stream()
                        .noneMatch(dto -> dto.getMbtiFactor() == zeroDto.getMbtiFactor()
                                && dto.getEmotion() == zeroDto.getEmotion()));

        Comparator<MBTIEmotionAmountAverageDto> byMbtiFactorAndEmotion = Comparator
                .comparing(MBTIEmotionAmountAverageDto::getMbtiFactor)
                .thenComparing(MBTIEmotionAmountAverageDto::getEmotion);

        return Stream.concat(dtos.stream(), zeroStream)
                .sorted(byMbtiFactorAndEmotion)
                .toList();
    }

    public MBTIDailyAmountSumResponse getAmountSumsEachMBTIAndDayLast30Days(RegisterType registerType) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(PERIOD_CRITERIA);
        List<MBTIDailyAmountSumDto> dtos = mbtiStatisticsRepository.getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(registerType, startDate, today);
        List<MBTIDailyAmountSumDto> dtosWithZeroPadding = addZeroAverageForMissingGenderLocalDatePairs(dtos);

        return MBTIDailyAmountSumResponse.builder()
                .mbti(authenticatedUserService.getUserMBTI())
                .mbtiDailyAmountSums(
                        dtosWithZeroPadding.stream()
                                .collect(
                                        groupingBy(MBTIDailyAmountSumDto::getMbtiFactor))
                                .entrySet().stream()
                                .map((e) ->
                                        MBTIDailyAmountSumResponse.MBTIDailyAmountSum.of(e.getKey(), e.getValue()))
                                .sorted(Comparator.comparing(MBTIDailyAmountSumResponse.MBTIDailyAmountSum::getMbtiFactor))
                                .toList())
                .build();
    }

    private List<MBTIDailyAmountSumDto> addZeroAverageForMissingGenderLocalDatePairs(List<MBTIDailyAmountSumDto> dtos) {
        Stream<LocalDate> localDateRanges = IntStream.rangeClosed(1, PERIOD_CRITERIA)
                .mapToObj(i -> LocalDate.now().minusDays(i));
        Stream<MBTIDailyAmountSumDto> zeroStream = localDateRanges
                .flatMap(d ->
                        Arrays.stream(MBTIFactor.values())
                                .map(f -> new MBTIDailyAmountSumDto(f, d, 0L)))
                .filter(zeroDto -> dtos.stream()
                        .noneMatch(dto -> dto.getMbtiFactor() == zeroDto.getMbtiFactor()
                                && dto.getLocalDate().equals(zeroDto.getLocalDate())));

        Comparator<MBTIDailyAmountSumDto> byMbtiFactorAndLocalDate = Comparator
                .comparing(MBTIDailyAmountSumDto::getMbtiFactor)
                .thenComparing(MBTIDailyAmountSumDto::getLocalDate);

        return Stream.concat(
                dtos.stream()
                        .filter(d ->
                                !d.getLocalDate().equals(LocalDate.now())),
                zeroStream)
                .sorted(byMbtiFactorAndLocalDate)
                .toList();
    }

    public MBTIWordFrequencyResponse getWordFrequenciesLast30Days(RegisterType registerType){
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(PERIOD_CRITERIA);
        // 최근 30일동안 모든 유저가 적은 메모의 빈도수 측정
        List<MemoDto> memos = mbtiStatisticsRepository.getAllMemosByMBTIBetweenStartDateAndEndDate(
                registerType,
                Mbti.NONE.toString(),
                startDate,
                today);

        // 최근 30일 동안 나와 MBTI가 같은 유저가 적은 메모의 빈도수 측정
        Mbti mbti = authenticatedUserService.getUserMBTI();

        if(isNone(mbti)){
            return MBTIWordFrequencyResponse.builder()
                    .mbti(mbti)
                    .allWordFrequencies(
                            wordExtractionService.analyzeWords(
                                    memos.stream()
                                            .flatMap((m) -> Stream.of(
                                                    m.getContent(),
                                                    m.getEvent(),
                                                    m.getReason(),
                                                    m.getThought(),
                                                    m.getImprovements()))
                                            .filter(Objects::nonNull)
                                            .toList())
                    )
                    .userWordFrequencies(List.of())
                    .build();
        }

        List<MemoDto> memoByMBTI = mbtiStatisticsRepository.getAllMemosByMBTIBetweenStartDateAndEndDate(
                registerType,
                mbti.toString(),
                startDate,
                today);

        return MBTIWordFrequencyResponse.builder()
                .mbti(mbti)
                .allWordFrequencies(
                        wordExtractionService.analyzeWords(
                                memos.stream()
                                        .flatMap((m) -> Stream.of(
                                                m.getContent(),
                                                m.getEvent(),
                                                m.getReason(),
                                                m.getThought(),
                                                m.getImprovements()))
                                        .filter(Objects::nonNull)
                                        .toList())
                )
                .userWordFrequencies(
                        wordExtractionService.analyzeWords(
                                memoByMBTI.stream()
                                        .flatMap((m) -> Stream.of(
                                                m.getContent(),
                                                m.getEvent(),
                                                m.getReason(),
                                                m.getThought(),
                                                m.getImprovements()))
                                        .filter(Objects::nonNull)
                                        .toList())
                )
                .build();
    }

    private static boolean isNone(Mbti mbti) {
        return mbti == null || mbti == Mbti.NONE;
    }

    public MBTISatisfactionAverageResponse getSatisfactionAveragesEachMBTILast30Days(RegisterType registerType){
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(PERIOD_CRITERIA);
        return MBTISatisfactionAverageResponse.builder()
                .mbti(authenticatedUserService.getUserMBTI())
                .mbtiSatisfactionAverages(
                        mbtiStatisticsRepository
                                .getSatisfactionAveragesEachMBTIBetweenStartDateAndEndDate(
                                        registerType,
                                        startDate,
                                        today))
                .build();
    }
}
