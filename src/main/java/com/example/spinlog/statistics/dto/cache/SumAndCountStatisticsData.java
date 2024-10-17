package com.example.spinlog.statistics.dto.cache;

import java.util.Map;

public record SumAndCountStatisticsData(
        Map<String, Object> sumData,
        Map<String, Object> countData) {
    @Override
    public String toString() {
        return "SumAndCountStatisticsData{" +
                "\n\tsumData=" + sumData +
                "\n\tcountData=" + countData +
                '}';
    }
}
