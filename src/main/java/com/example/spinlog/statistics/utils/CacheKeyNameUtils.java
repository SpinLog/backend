package com.example.spinlog.statistics.utils;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.user.entity.Gender;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheKeyNameUtils {
    public static String GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(RegisterType registerType) {
        return "GenderEmotionStatisticsAmountSum::" + registerType;
    }

    public static String GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(RegisterType registerType) {
        return "GenderEmotionStatisticsAmountCount::" + registerType;
    }

    public static String GENDER_DAILY_AMOUNT_SUM_KEY_NAME(RegisterType registerType) {
        return "GenderDailyStatisticsAmountSum::" + registerType;
    }

    public static String GENDER_SATISFACTION_SUM_KEY_NAME(RegisterType registerType) {
        return "GenderStatisticsSatisfactionSum::" + registerType;
    }

    public static String GENDER_SATISFACTION_COUNT_KEY_NAME(RegisterType registerType) {
        return "GenderStatisticsSatisfactionCount::" + registerType;
    }

    public static List<String> getGenderEmotionKeys() {
        return Arrays.stream(Gender.values())
                .filter(g -> !g.equals(Gender.NONE))
                .flatMap(g ->
                        Arrays.stream(Emotion.values())
                                .map(e -> g + "::" + e))
                .toList();
    }

    public static List<String> getGenderDailyKeys(StatisticsPeriodManager.Period period) {
        LocalDate startDate = period.startDate();
        LocalDate endDate = period.endDate();
        List<String> keys = new ArrayList<>();
        List<Gender> genders = Arrays.stream(Gender.values())
                .filter(g -> !g.equals(Gender.NONE))
                .toList();
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            String dateString = date.toString();
            genders.forEach(g -> keys.add(g + "::" + dateString));
        }
        return keys;
    }

    public static List<String> getGenderKeys() {
        return Arrays.stream(Gender.values())
                .filter(g -> !g.equals(Gender.NONE))
                .map(Gender::name)
                .toList();
    }

    public static List<String> getMBTIEmotionKeys() {
        return Arrays.stream(MBTIFactor.values())
                .flatMap(f ->
                        Arrays.stream(Emotion.values())
                                .map(e -> f + "::" + e))
                .toList();
    }

    public static List<String> getMBTIDailyKeys(StatisticsPeriodManager.Period period) {
        LocalDate startDate = period.startDate();
        LocalDate endDate = period.endDate();
        List<String> keys = new ArrayList<>();
        List<MBTIFactor> factors = Arrays.stream(MBTIFactor.values())
                .toList();
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            String dateString = date.toString();
            factors.forEach(f -> keys.add(f + "::" + dateString));
        }
        return keys;
    }

    public static List<String> getMBTIKeys() {
        return Arrays.stream(MBTIFactor.values())
                .map(MBTIFactor::name)
                .toList();
    }

    public static String MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(RegisterType registerType) {
        return "MBTIEmotionStatisticsAmountSum::" + registerType;
    }

    public static String MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(RegisterType registerType) {
        return "MBTIEmotionStatisticsAmountCount::" + registerType;
    }

    public static String MBTI_DAILY_AMOUNT_SUM_KEY_NAME(RegisterType registerType) {
        return "MBTIDailyStatisticsAmountSum::" + registerType;
    }

    public static String MBTI_SATISFACTION_SUM_KEY_NAME(RegisterType registerType) {
        return "MBTIStatisticsSatisfactionSum::" + registerType;
    }

    public static String MBTI_SATISFACTION_COUNT_KEY_NAME(RegisterType registerType) {
        return "MBTIStatisticsSatisfactionCount::" + registerType;
    }
}
