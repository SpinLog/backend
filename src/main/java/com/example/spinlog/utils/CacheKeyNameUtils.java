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
    public static String getGenderEmotionStatisticsAmountSumKeyName(RegisterType registerType) {
        return "GenderEmotionStatisticsAmountSum::" + registerType;
    }

    public static String getGenderEmotionStatisticsAmountCountKeyName(RegisterType registerType) {
        return "GenderEmotionStatisticsAmountCount::" + registerType;
    }

    public static String getGenderDailyStatisticsAmountSumKeyName(RegisterType registerType) {
        return "GenderDailyStatisticsAmountSum::" + registerType;
    }

    public static String getGenderStatisticsSatisfactionSumKeyName(RegisterType registerType) {
        return "GenderStatisticsSatisfactionSum::" + registerType;
    }

    public static String getGenderStatisticsSatisfactionCountKeyName(RegisterType registerType) {
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
