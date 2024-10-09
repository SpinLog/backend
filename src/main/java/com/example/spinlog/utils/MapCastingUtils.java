package com.example.spinlog.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapCastingUtils {
    public static Map<String, Object> convertValuesToLong(Map<String, Object> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Long.parseLong(entry.getValue().toString())));
    }

    public static Map<String, Object> convertValuesToDouble(Map<String, Object> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Double.parseDouble(entry.getValue().toString())));
    }
}
