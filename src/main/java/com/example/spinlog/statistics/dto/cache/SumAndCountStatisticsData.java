package com.example.spinlog.statistics.dto.cache;

import java.util.Map;

public record SumAndCountStatisticsData<T extends Number>(
        Map<String, T> sumData, // must be Long or Double
        Map<String, Long> countData) {
    @Override
    public String toString() {
        return "SumAndCountStatisticsData{" +
                "\n\tsumData=" + sumData +
                "\n\tcountData=" + countData +
                '}';
    }
}
