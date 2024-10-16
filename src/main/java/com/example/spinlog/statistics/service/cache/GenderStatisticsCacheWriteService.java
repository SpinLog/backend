package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.AllStatisticsMap;
import com.example.spinlog.statistics.dto.cache.CountsAndSums;
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

    public void putAmountCountsAndSumsByGenderAndEmotion(CountsAndSums amountCountsAndSums, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType), amountCountsAndSums.countsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType), amountCountsAndSums.sumsMap());
    }

    public void putAmountSumsByGenderAndDate(Map<String, Object> amountSums, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType), amountSums);
    }

    public void putSatisfactionCountsAndSumsByGender(CountsAndSums satisfactionCountsAndSums, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(registerType), satisfactionCountsAndSums.countsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(registerType), satisfactionCountsAndSums.sumsMap());
    }

    public void decrementAllData(AllStatisticsMap statisticsAllData) {
        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().countsMap());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().countsMap());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().sumsMap());

        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().countsMap());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().countsMap());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().sumsMap());
    }

    public void incrementAllData(AllStatisticsMap statisticsAllData) {
        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().countsMap());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendCountsAndSums().sumsMap());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().countsMap());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendCountsAndSums().sumsMap());

        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().countsMap());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveCountsAndSums().sumsMap());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().countsMap());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveCountsAndSums().sumsMap());
    }

    public void replaceAmountCountsAndSumsByGenderAndEmotion(CountsAndSums amountCountsAndSums, RegisterType registerType) {
        cacheHashRepository.deleteData(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType));
        cacheHashRepository.deleteData(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType));
        putAmountCountsAndSumsByGenderAndEmotion(amountCountsAndSums, registerType);
    }

    public void replaceAmountSumsByGenderAndDate(Map<String, Object> amountSums, RegisterType registerType) {
        cacheHashRepository.deleteData(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType));
        putAmountSumsByGenderAndDate(amountSums, registerType);
    }

    public void replaceSatisfactionCountsAndSumsByGender(CountsAndSums satisfactionCountsAndSums, RegisterType registerType) {
        cacheHashRepository.deleteData(GENDER_SATISFACTION_COUNT_KEY_NAME(registerType));
        cacheHashRepository.deleteData(GENDER_SATISFACTION_SUM_KEY_NAME(registerType));
        putSatisfactionCountsAndSumsByGender(satisfactionCountsAndSums, registerType);
    }
}