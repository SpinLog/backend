package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.MapCastingUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MBTIStatisticsCacheFetchService {
    private final CacheHashRepository cacheHashRepository;

    public SumAndCountStatisticsData<Long> getAmountAveragesEachMBTIAndEmotion(RegisterType registerType) {
        Map<String, Object> sumData = cacheHashRepository.getHashEntries(
                MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType));
        Map<String, Long> castedSumData = convertValuesToLong(sumData);

        Map<String, Object> countsMap = cacheHashRepository.getHashEntries(
                MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType));
        Map<String, Long> castedCountData = convertValuesToLong(countsMap);

        return new SumAndCountStatisticsData<>(
                castedSumData, castedCountData);
    }

    public Map<String, Long> getAmountSumsEachMBTIAndDay(RegisterType registerType) {
        Map<String, Object> sumData = cacheHashRepository.getHashEntries(
                MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType));
        return convertValuesToLong(sumData);
    }

    public SumAndCountStatisticsData<Double> getSatisfactionAveragesEachMBTI(RegisterType registerType) {
        Map<String, Object> sumData = cacheHashRepository.getHashEntries(
                MBTI_SATISFACTION_SUM_KEY_NAME(registerType));
        Map<String, Double> castedSumData = convertValuesToDouble(sumData);

        Map<String, Object> countData = cacheHashRepository.getHashEntries(
                MBTI_SATISFACTION_COUNT_KEY_NAME(registerType));
        Map<String, Long> castedCountData = convertValuesToLong(countData);

        return new SumAndCountStatisticsData<>(
                castedSumData, castedCountData);
    }
}
