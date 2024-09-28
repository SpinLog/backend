package com.example.spinlog.statistics.service;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.*;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsCacheFetchService;
import com.example.spinlog.statistics.service.dto.GenderDailyAmountSumResponse;
import com.example.spinlog.statistics.service.dto.GenderEmotionAmountAverageResponse;
import com.example.spinlog.statistics.service.dto.GenderWordFrequencyResponse;
import com.example.spinlog.statistics.service.wordanalysis.WordExtractionService;
import com.example.spinlog.user.entity.Gender;
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

import static com.example.spinlog.utils.StatisticsCacheUtils.PERIOD_CRITERIA;
import static java.util.stream.Collectors.groupingBy;

@Slf4j
@Service
@Transactional(readOnly = true) // todo 범위 좁히기
public class GenderStatisticsService {
    private final GenderStatisticsRepository genderStatisticsRepository;
    private final GenderStatisticsCacheFetchService genderStatisticsCacheFetchService;
    private final WordExtractionService wordExtractionService;

    public GenderStatisticsService(GenderStatisticsRepository genderStatisticsRepository, GenderStatisticsCacheFetchService genderStatisticsCacheFetchService, WordExtractionService wordExtractionService) {
        this.genderStatisticsRepository = genderStatisticsRepository;
        this.genderStatisticsCacheFetchService = genderStatisticsCacheFetchService;
        this.wordExtractionService = wordExtractionService;
    }

    public List<GenderEmotionAmountAverageResponse> getAmountAveragesEachGenderAndEmotionLast30Days(RegisterType registerType){
        List<GenderEmotionAmountAverageDto> dtos = genderStatisticsCacheFetchService.
                getAmountAveragesEachGenderAndEmotion(registerType);

        // TODO 데이터 프로세싱 작업 별도 클래스로 분리
        List<GenderEmotionAmountAverageDto> dtosWithZeroPadding = addZeroAverageForMissingGenderEmotionPairs(dtos);

        return dtosWithZeroPadding.stream()
                .collect(
                        groupingBy(GenderEmotionAmountAverageDto::getGender))
                .entrySet().stream()
                .map((e) ->
                        GenderEmotionAmountAverageResponse.of(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(GenderEmotionAmountAverageResponse::getGender))
                .toList();
    }

    private List<GenderEmotionAmountAverageDto> addZeroAverageForMissingGenderEmotionPairs(List<GenderEmotionAmountAverageDto> dtos) {
        Stream<GenderEmotionAmountAverageDto> zeroStream = Arrays.stream(Emotion.values())
                .flatMap(e ->
                        Arrays.stream(Gender.values())
                                .filter(g -> !g.equals(Gender.NONE))
                                .map(g -> new GenderEmotionAmountAverageDto(g, e, 0L)))
                .filter(zeroDto -> dtos.stream()
                        .noneMatch(dto -> dto.getGender() == zeroDto.getGender()
                                && dto.getEmotion() == zeroDto.getEmotion()));

        Comparator<GenderEmotionAmountAverageDto> byGenderAndEmotion = Comparator
                .comparing(GenderEmotionAmountAverageDto::getGender)
                .thenComparing(GenderEmotionAmountAverageDto::getEmotion);

        return Stream.concat(dtos.stream(), zeroStream)
                .sorted(byGenderAndEmotion)
                .toList();
    }

    public List<GenderDailyAmountSumResponse> getAmountSumsEachGenderAndDayLast30Days(RegisterType registerType) {
        List<GenderDailyAmountSumDto> dtos = genderStatisticsCacheFetchService
                .getAmountSumsEachGenderAndDay(registerType);

        List<GenderDailyAmountSumDto> dtosWithZeroPadding = addZeroAverageForMissingGenderLocalDatePairs(dtos);

        return dtosWithZeroPadding.stream()
                .collect(
                        groupingBy(GenderDailyAmountSumDto::getGender))
                .entrySet().stream()
                .map((e) ->
                        GenderDailyAmountSumResponse.of(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(GenderDailyAmountSumResponse::getGender))
                .toList();
    }

    private List<GenderDailyAmountSumDto> addZeroAverageForMissingGenderLocalDatePairs(List<GenderDailyAmountSumDto> dtos) {
        Stream<LocalDate> localDateRanges = IntStream.rangeClosed(1, PERIOD_CRITERIA)
                .mapToObj(i -> LocalDate.now().minusDays(i));
        Stream<GenderDailyAmountSumDto> zeroStream = localDateRanges
                .flatMap(d ->
                        Arrays.stream(Gender.values())
                                .filter(g -> !g.equals(Gender.NONE))
                                .map(g -> new GenderDailyAmountSumDto(g, d, 0L)))
                .filter(zeroDto -> dtos.stream()
                        .noneMatch(dto -> dto.getGender() == zeroDto.getGender()
                                && dto.getLocalDate().equals(zeroDto.getLocalDate())));

        Comparator<GenderDailyAmountSumDto> byGenderAndLocalDate = Comparator
                .comparing(GenderDailyAmountSumDto::getGender)
                .thenComparing(GenderDailyAmountSumDto::getLocalDate);

        return Stream.concat(
                dtos.stream()
                        .filter(d ->
                                !d.getLocalDate().equals(LocalDate.now())),
                zeroStream)
                .sorted(byGenderAndLocalDate)
                .toList();
    }

    public GenderWordFrequencyResponse getWordFrequenciesEachGenderLast30Days(RegisterType registerType){
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(PERIOD_CRITERIA);
        List<MemoDto> maleMemos = genderStatisticsRepository.getAllMemosByGenderBetweenStartDateAndEndDate(registerType, Gender.MALE, startDate, today);
        List<MemoDto> femaleMemos = genderStatisticsRepository.getAllMemosByGenderBetweenStartDateAndEndDate(registerType, Gender.FEMALE, startDate, today);

        return GenderWordFrequencyResponse.builder()
                .maleWordFrequencies(
                        wordExtractionService.analyzeWords(
                                maleMemos.stream()
                                        .flatMap((m) -> Stream.of(
                                                m.getContent(),
                                                m.getEvent(),
                                                m.getReason(),
                                                m.getThought(),
                                                m.getImprovements()))
                                        .filter(Objects::nonNull)
                                        .toList()))
                .femaleWordFrequencies(
                        wordExtractionService.analyzeWords(
                                femaleMemos.stream()
                                        .flatMap((m) -> Stream.of(
                                                m.getContent(),
                                                m.getEvent(),
                                                m.getReason(),
                                                m.getThought(),
                                                m.getImprovements()))
                                        .filter(Objects::nonNull)
                                        .toList()))
                .build();
    }

    private static boolean isNone(Mbti mbti) {
        return mbti == null || mbti == Mbti.NONE;
    }

    public List<GenderSatisfactionAverageDto> getSatisfactionAveragesEachGenderLast30Days(RegisterType registerType){
        return genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(registerType);
    }
}
