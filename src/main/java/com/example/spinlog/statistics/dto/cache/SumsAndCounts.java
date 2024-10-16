package com.example.spinlog.statistics.dto.cache;

import java.util.Map;

public record SumsAndCounts(
        Map<String, Object> sumsMap,
        Map<String, Object> countsMap) {
    @Override
    public String toString() {
        return "CountsAndSums{" +
                "\n\tsumsMap=" + sumsMap +
                "\n\tcountsMap=" + countsMap +
                '}';
    }
}
