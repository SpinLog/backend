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

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GenderStatisticsCacheWriteService {
    private final CacheHashRepository cacheHashRepository;

    public void putAmountCountsAndSumsByGenderAndEmotion(SumAndCountStatisticsData<Long> amountSumAndCountStatisticsData, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType), amountSumAndCountStatisticsData.countData());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType), amountSumAndCountStatisticsData.sumData());
    }

    public void putAmountSumsByGenderAndDate(Map<String, Long> amountSums, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType), amountSums);
    }

    public void putSatisfactionCountsAndSumsByGender(SumAndCountStatisticsData<Double> satisfactionSumAndCountStatisticsData, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(registerType), satisfactionSumAndCountStatisticsData.countData());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(registerType), satisfactionSumAndCountStatisticsData.sumData());
    }

    public void decrementAllData(AllStatisticsCacheData statisticsAllData) {
        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.emotionAmountSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.emotionAmountSpendSumAndCountStatisticsData().sumData());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.dailyAmountSpendSums());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.satisfactionSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.satisfactionSpendSumAndCountStatisticsData().sumData());

        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.emotionAmountSaveSumAndCountStatisticsData().countData());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.emotionAmountSaveSumAndCountStatisticsData().sumData());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.dailyAmountSaveSums());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.satisfactionSaveSumAndCountStatisticsData().countData());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.satisfactionSaveSumAndCountStatisticsData().sumData());
    }

    public void incrementAllData(AllStatisticsCacheData statisticsAllData) {
        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.emotionAmountSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.emotionAmountSpendSumAndCountStatisticsData().sumData());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.dailyAmountSpendSums());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.satisfactionSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.satisfactionSpendSumAndCountStatisticsData().sumData());

        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.emotionAmountSaveSumAndCountStatisticsData().countData());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.emotionAmountSaveSumAndCountStatisticsData().sumData());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.dailyAmountSaveSums());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.satisfactionSaveSumAndCountStatisticsData().countData());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.satisfactionSaveSumAndCountStatisticsData().sumData());
    }

    public void replaceAmountCountsAndSumsByGenderAndEmotion(SumAndCountStatisticsData<Long> amountSumAndCountStatisticsData, RegisterType registerType) {
        cacheHashRepository.deleteData(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType));
        cacheHashRepository.deleteData(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType));
        putAmountCountsAndSumsByGenderAndEmotion(amountSumAndCountStatisticsData, registerType);
    }

    public void replaceAmountSumsByGenderAndDate(Map<String, Long> amountSums, RegisterType registerType) {
        cacheHashRepository.deleteData(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType));
        putAmountSumsByGenderAndDate(amountSums, registerType);
    }

    public void replaceSatisfactionCountsAndSumsByGender(SumAndCountStatisticsData<Double> satisfactionSumAndCountStatisticsData, RegisterType registerType) {
        cacheHashRepository.deleteData(GENDER_SATISFACTION_COUNT_KEY_NAME(registerType));
        cacheHashRepository.deleteData(GENDER_SATISFACTION_SUM_KEY_NAME(registerType));
        putSatisfactionCountsAndSumsByGender(satisfactionSumAndCountStatisticsData, registerType);
    }
}