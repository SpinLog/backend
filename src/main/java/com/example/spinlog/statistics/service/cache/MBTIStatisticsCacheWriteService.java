package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class MBTIStatisticsCacheWriteService {
    private final CacheHashRepository cacheHashRepository;

    public void putAmountCountsAndSumsByMBTIAndEmotion(SumAndCountStatisticsData<Long> amountSumAndCountStatisticsData, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType), amountSumAndCountStatisticsData.countData());
        cacheHashRepository.putAllDataInHash(
                MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType), amountSumAndCountStatisticsData.sumData());
    }

    public void putAmountSumsByMBTIAndDate(Map<String, Long> amountSums, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType), amountSums);
    }

    public void putSatisfactionCountsAndSumsByMBTI(SumAndCountStatisticsData<Double> satisfactionSumAndCountStatisticsData, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                MBTI_SATISFACTION_COUNT_KEY_NAME(registerType), satisfactionSumAndCountStatisticsData.countData());
        cacheHashRepository.putAllDataInHash(
                MBTI_SATISFACTION_SUM_KEY_NAME(registerType), satisfactionSumAndCountStatisticsData.sumData());
    }
}
