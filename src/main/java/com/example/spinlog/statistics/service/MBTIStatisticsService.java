package com.example.spinlog.statistics.service;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.dto.repository.MBTIDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.MBTIEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.repository.MBTISatisfactionAverageDto;
import com.example.spinlog.statistics.dto.repository.MemoDto;
import com.example.spinlog.statistics.dto.response.MBTIDailyAmountSumResponse;
import com.example.spinlog.statistics.dto.response.MBTIEmotionAmountAverageResponse;
import com.example.spinlog.statistics.dto.response.MBTISatisfactionAverageResponse;
import com.example.spinlog.statistics.dto.response.MBTIWordFrequencyResponse;
import com.example.spinlog.statistics.repository.MBTIStatisticsRepository;
import com.example.spinlog.statistics.loginService.AuthenticatedUserService;
import com.example.spinlog.statistics.service.cache.MBTIStatisticsCacheFallbackService;
import com.example.spinlog.statistics.service.wordanalysis.WordExtractionService;
import com.example.spinlog.user.entity.Mbti;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.PERIOD_CRITERIA;
import static com.example.spinlog.statistics.utils.StatisticsZeroPaddingUtils.*;
import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class MBTIStatisticsService {
    private final MBTIStatisticsRepository mbtiStatisticsRepository;
    private final MBTIStatisticsCacheFallbackService mbtiStatisticsCacheFallbackService;
    private final WordExtractionService wordExtractionService;
    private final AuthenticatedUserService authenticatedUserService;

    public MBTIEmotionAmountAverageResponse getAmountAveragesEachMBTIAndEmotionLast30Days(RegisterType registerType){
        List<MBTIEmotionAmountAverageDto> dtos = mbtiStatisticsCacheFallbackService.getAmountAveragesEachMBTIAndEmotion(registerType);
        List<MBTIEmotionAmountAverageDto> dtosWithZeroPadding = zeroPaddingToMBTIEmotionAmountAverageDtoList(dtos);

        return MBTIEmotionAmountAverageResponse.of(
                authenticatedUserService.getUserMBTI(),
                dtosWithZeroPadding);
    }

    public MBTIDailyAmountSumResponse getAmountSumsEachMBTIAndDayLast30Days(RegisterType registerType) {
        List<MBTIDailyAmountSumDto> dtos = mbtiStatisticsCacheFallbackService.getAmountSumsEachMBTIAndDay(registerType);
        List<MBTIDailyAmountSumDto> dtosWithZeroPadding = zeroPaddingToMBTIDailyAmountSumDtoList(dtos);

        return MBTIDailyAmountSumResponse.of(
                authenticatedUserService.getUserMBTI(),
                dtosWithZeroPadding);
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
        List<MBTISatisfactionAverageDto> dtos = mbtiStatisticsCacheFallbackService.getSatisfactionAveragesEachMBTI(registerType);
        return MBTISatisfactionAverageResponse.of(
                authenticatedUserService.getUserMBTI(),
                dtos);
    }
}
