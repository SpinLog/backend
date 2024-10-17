package com.example.spinlog.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class MapCastingUtilsTest {

    @Test
    void Map의_value_들을_Long_타입으로_변환한다() throws Exception {
        // given
        Map<String, Object> map = Map.of(
                "key1", "1",
                "key2", "2",
                "key3", "3");

        // when
        Map<String, Long> convertedMap = MapCastingUtils.convertValuesToLong(map);

        // then
        for(var e: convertedMap.entrySet()) {
            assertThat(e.getValue()).isInstanceOf(Long.class);
        }
        System.out.println(convertedMap);
    }

    @Test
    void Map의_value_들을_Double_타입으로_변환한다() throws Exception {
        // given
        Map<String, Object> map = Map.of(
                "key1", "1.1",
                "key2", "2.2",
                "key3", "3.3");

        // when
        Map<String, Double> convertedMap = MapCastingUtils.convertValuesToDouble(map);

        // then
        for(var e: convertedMap.entrySet()) {
            assertThat(e.getValue()).isInstanceOf(Double.class);
        }
        System.out.println(convertedMap);
    }

}