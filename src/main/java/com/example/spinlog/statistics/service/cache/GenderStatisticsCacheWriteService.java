package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.AllGenderStatisticsCacheData;
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

    public void putAmountCountsAndSumsByGenderAndEmotion(SumAndCountStatisticsData amountSumAndCountStatisticsData, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType), amountSumAndCountStatisticsData.countData());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType), amountSumAndCountStatisticsData.sumData());
    }

    public void putAmountSumsByGenderAndDate(Map<String, Object> amountSums, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType), amountSums);
    }

    public void putSatisfactionCountsAndSumsByGender(SumAndCountStatisticsData satisfactionSumAndCountStatisticsData, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(registerType), satisfactionSumAndCountStatisticsData.countData());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(registerType), satisfactionSumAndCountStatisticsData.sumData());
    }

    public void decrementAllData(AllGenderStatisticsCacheData statisticsAllData) {
        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumAndCountStatisticsData().sumData());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumAndCountStatisticsData().sumData());

        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumAndCountStatisticsData().countData());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumAndCountStatisticsData().sumData());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumAndCountStatisticsData().countData());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumAndCountStatisticsData().sumData());
    }

    public void incrementAllData(AllGenderStatisticsCacheData statisticsAllData) {
        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumAndCountStatisticsData().sumData());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumAndCountStatisticsData().countData());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumAndCountStatisticsData().sumData());

        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumAndCountStatisticsData().countData());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumAndCountStatisticsData().sumData());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumAndCountStatisticsData().countData());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumAndCountStatisticsData().sumData());
    }

    public void replaceAmountCountsAndSumsByGenderAndEmotion(SumAndCountStatisticsData amountSumAndCountStatisticsData, RegisterType registerType) {
        cacheHashRepository.deleteData(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType));
        cacheHashRepository.deleteData(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType));
        putAmountCountsAndSumsByGenderAndEmotion(amountSumAndCountStatisticsData, registerType);
    }

    public void replaceAmountSumsByGenderAndDate(Map<String, Object> amountSums, RegisterType registerType) {
        cacheHashRepository.deleteData(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType));
        putAmountSumsByGenderAndDate(amountSums, registerType);
    }

    public void replaceSatisfactionCountsAndSumsByGender(SumAndCountStatisticsData satisfactionSumAndCountStatisticsData, RegisterType registerType) {
        cacheHashRepository.deleteData(GENDER_SATISFACTION_COUNT_KEY_NAME(registerType));
        cacheHashRepository.deleteData(GENDER_SATISFACTION_SUM_KEY_NAME(registerType));
        putSatisfactionCountsAndSumsByGender(satisfactionSumAndCountStatisticsData, registerType);
    }
}