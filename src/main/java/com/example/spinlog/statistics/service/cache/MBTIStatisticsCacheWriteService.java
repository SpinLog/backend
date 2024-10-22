package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.SAVE;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
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

    public void decrementAllData(AllStatisticsCacheData statisticsAllData) {
        cacheHashRepository.decrementAllDataInHash(
                MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.emotionAmountSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.decrementAllDataInHash(
                MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.emotionAmountSpendSumAndCountStatisticsData().sumData());
        cacheHashRepository.decrementAllDataInHash(
                MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.dailyAmountSpendSums());
        cacheHashRepository.decrementAllDataInHash(
                MBTI_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.satisfactionSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.decrementAllDataInHash(
                MBTI_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.satisfactionSpendSumAndCountStatisticsData().sumData());

        cacheHashRepository.decrementAllDataInHash(
                MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.emotionAmountSaveSumAndCountStatisticsData().countData());
        cacheHashRepository.decrementAllDataInHash(
                MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.emotionAmountSaveSumAndCountStatisticsData().sumData());
        cacheHashRepository.decrementAllDataInHash(
                MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.dailyAmountSaveSums());
        cacheHashRepository.decrementAllDataInHash(
                MBTI_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.satisfactionSaveSumAndCountStatisticsData().countData());
        cacheHashRepository.decrementAllDataInHash(
                MBTI_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.satisfactionSaveSumAndCountStatisticsData().sumData());
    }

    public void incrementAllData(AllStatisticsCacheData statisticsAllData) {
        cacheHashRepository.incrementAllDataInHash(
                MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.emotionAmountSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.incrementAllDataInHash(
                MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.emotionAmountSpendSumAndCountStatisticsData().sumData());
        cacheHashRepository.incrementAllDataInHash(
                MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.dailyAmountSpendSums());
        cacheHashRepository.incrementAllDataInHash(
                MBTI_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.satisfactionSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.incrementAllDataInHash(
                MBTI_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.satisfactionSpendSumAndCountStatisticsData().sumData());

        cacheHashRepository.incrementAllDataInHash(
                MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.emotionAmountSaveSumAndCountStatisticsData().countData());
        cacheHashRepository.incrementAllDataInHash(
                MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.emotionAmountSaveSumAndCountStatisticsData().sumData());
        cacheHashRepository.incrementAllDataInHash(
                MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.dailyAmountSaveSums());
        cacheHashRepository.incrementAllDataInHash(
                MBTI_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.satisfactionSaveSumAndCountStatisticsData().countData());
        cacheHashRepository.incrementAllDataInHash(
                MBTI_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.satisfactionSaveSumAndCountStatisticsData().sumData());
    }

    public void replaceAmountCountsAndSumsByMBTIAndEmotion(SumAndCountStatisticsData<Long> repositoryData, RegisterType registerType) {
        cacheHashRepository.deleteData(MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType));
        cacheHashRepository.deleteData(MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType));
        putAmountCountsAndSumsByMBTIAndEmotion(repositoryData, registerType);
    }

    public void replaceAmountSumsByMBTIAndDate(Map<String, Long> repositoryData, RegisterType registerType) {
        cacheHashRepository.deleteData(MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType));
        putAmountSumsByMBTIAndDate(repositoryData, registerType);
    }

    public void replaceSatisfactionCountsAndSumsByMBTI(SumAndCountStatisticsData<Double> repositoryData, RegisterType registerType) {
        cacheHashRepository.deleteData(MBTI_SATISFACTION_COUNT_KEY_NAME(registerType));
        cacheHashRepository.deleteData(MBTI_SATISFACTION_SUM_KEY_NAME(registerType));
        putSatisfactionCountsAndSumsByMBTI(repositoryData, registerType);
    }
}
