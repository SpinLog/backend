package com.example.spinlog.utils;

import com.example.spinlog.article.entity.RegisterType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
}
