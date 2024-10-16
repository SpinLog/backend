package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.AllGenderStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumsAndCounts;
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

    public void putAmountCountsAndSumsByGenderAndEmotion(SumsAndCounts amountSumsAndCounts, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType), amountSumsAndCounts.countsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType), amountSumsAndCounts.sumsMap());
    }

    public void putAmountSumsByGenderAndDate(Map<String, Object> amountSums, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType), amountSums);
    }

    public void putSatisfactionCountsAndSumsByGender(SumsAndCounts satisfactionSumsAndCounts, RegisterType registerType) {
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(registerType), satisfactionSumsAndCounts.countsMap());
        cacheHashRepository.putAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(registerType), satisfactionSumsAndCounts.sumsMap());
    }

    public void decrementAllData(AllGenderStatisticsCacheData statisticsAllData) {
        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumsAndCounts().countsMap());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumsAndCounts().sumsMap());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumsAndCounts().countsMap());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumsAndCounts().sumsMap());

        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumsAndCounts().countsMap());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumsAndCounts().sumsMap());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumsAndCounts().countsMap());
        cacheHashRepository.decrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumsAndCounts().sumsMap());
    }

    public void incrementAllData(AllGenderStatisticsCacheData statisticsAllData) {
        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumsAndCounts().countsMap());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderEmotionAmountSpendSumsAndCounts().sumsMap());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), statisticsAllData.genderDailyAmountSpendSums());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumsAndCounts().countsMap());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND), statisticsAllData.genderSatisfactionSpendSumsAndCounts().sumsMap());

        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumsAndCounts().countsMap());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderEmotionAmountSaveSumsAndCounts().sumsMap());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), statisticsAllData.genderDailyAmountSaveSums());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumsAndCounts().countsMap());
        cacheHashRepository.incrementAllDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SAVE), statisticsAllData.genderSatisfactionSaveSumsAndCounts().sumsMap());
    }

    public void replaceAmountCountsAndSumsByGenderAndEmotion(SumsAndCounts amountSumsAndCounts, RegisterType registerType) {
        cacheHashRepository.deleteData(GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType));
        cacheHashRepository.deleteData(GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType));
        putAmountCountsAndSumsByGenderAndEmotion(amountSumsAndCounts, registerType);
    }

    public void replaceAmountSumsByGenderAndDate(Map<String, Object> amountSums, RegisterType registerType) {
        cacheHashRepository.deleteData(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType));
        putAmountSumsByGenderAndDate(amountSums, registerType);
    }

    public void replaceSatisfactionCountsAndSumsByGender(SumsAndCounts satisfactionSumsAndCounts, RegisterType registerType) {
        cacheHashRepository.deleteData(GENDER_SATISFACTION_COUNT_KEY_NAME(registerType));
        cacheHashRepository.deleteData(GENDER_SATISFACTION_SUM_KEY_NAME(registerType));
        putSatisfactionCountsAndSumsByGender(satisfactionSumsAndCounts, registerType);
    }
}