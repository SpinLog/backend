package com.example.spinlog.utils;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.user.entity.Gender;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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

    public static List<String> getGenderEmotionHashKeyNames(){
        Emotion[] emotions = Emotion.values();
        Gender[] genders = Gender.values();
        return Arrays.stream(genders)
                .filter(g -> !g.equals(Gender.NONE))
                .flatMap(g -> {
                    return Arrays.stream(emotions)
                            .map(e -> g + "::" + e);
                }).toList();
    }
}
