package com.example.spinlog.utils;

import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.*;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.utils.StatisticsZeroPaddingUtils.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StatisticsZeroPaddingUtilsTest {
    @Nested
    class zeroPaddingToGenderEmotionAmountSumAndCountStatisticsData {
        @Test
        void GenderEmotionAmountCountsAndSums에_대해_zero_padding을_수행한다() throws Exception {
            // given
            Map<String, Long> sumsMap = new HashMap<>();
            sumsMap.put("MALE::SAD", 10L);
            Map<String, Long> countsMap = new HashMap<>();
            countsMap.put("MALE::SAD", 1L);
            SumAndCountStatisticsData<Long> sumAndCountStatisticsData = new SumAndCountStatisticsData<>(sumsMap, countsMap);

            // when
            sumAndCountStatisticsData = zeroPaddingToEmotionAmountCountsAndSums(sumAndCountStatisticsData, getGenderEmotionKeys());

            // then
            List<String> genderEmotionKeys = getGenderEmotionKeys();
            assertThat(sumAndCountStatisticsData.countData().size()).isEqualTo(genderEmotionKeys.size());
            assertThat(sumAndCountStatisticsData.sumData().size()).isEqualTo(genderEmotionKeys.size());

            for (var key : genderEmotionKeys) {
                if (key.equals("MALE::SAD")) {
                    assertThat(sumAndCountStatisticsData.sumData().get(key)).isEqualTo(10L);
                    assertThat(sumAndCountStatisticsData.countData().get(key)).isEqualTo(1L);
                } else {
                    assertThat(sumAndCountStatisticsData.sumData().get(key)).isEqualTo(0L);
                    assertThat(sumAndCountStatisticsData.countData().get(key)).isEqualTo(0L);
                }
            }
        }

        @Test
        void 유효하지_않은_키가_있다면_실패한다() throws Exception {
            // given
            Map<String, Long> sumsMap = new HashMap<>();
            sumsMap.put("MAL::SAD", 10L);
            Map<String, Long> countsMap = new HashMap<>();
            countsMap.put("MALE::SADD", 1L);
            SumAndCountStatisticsData<Long> sumAndCountStatisticsData = new SumAndCountStatisticsData<>(sumsMap, countsMap);

            // when // then
            assertThatThrownBy(() -> zeroPaddingToEmotionAmountCountsAndSums(sumAndCountStatisticsData, getGenderEmotionKeys()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class zeroPaddingToGenderDailyAmountSums {
        @Test
        void GenderDailyAmountSums에_대해_zero_padding을_수행한다() throws Exception {
            // given
            Map<String, Long> sums = new HashMap<>();
            sums.put("MALE::2021-01-01", 10L);
            Period period = new Period(
                    LocalDate.of(2021, 1, 1),
                    LocalDate.of(2021, 1, 3));

            // when
            sums = zeroPaddingToAmountSums(sums, getGenderDailyKeys(period));

            // then
            List<String> genderDailyAmountKeys = getGenderDailyKeys(period);
            assertThat(sums.size()).isEqualTo(genderDailyAmountKeys.size());
            for(var key : genderDailyAmountKeys){
                if(key.equals("MALE::2021-01-01")){
                    assertThat(sums.get(key)).isEqualTo(10L);
                } else {
                    assertThat(sums.get(key)).isEqualTo(0L);
                }
            }
        }

        @Test
        void 유효하지_않은_키가_있다면_실패한다() throws Exception {
            // given
            Map<String, Long> sums = new HashMap<>();
            sums.put("MALE::22021-01-01", 10L);
            Period period = new Period(
                    LocalDate.of(2021, 1, 1),
                    LocalDate.of(2021, 1, 3));

            // when // then
            assertThatThrownBy(() -> zeroPaddingToAmountSums(sums, getGenderDailyKeys(period)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

    }

    @Nested
    class zeroPaddingToGenderSatisfactionAmountSumAndCountStatisticsData {
        @Test
        void GenderSatisfactionAmountCountsAndSums에_대해_zero_padding을_수행한다() throws Exception {
            // given
            Map<String, Double> sumsMap = new HashMap<>();
            sumsMap.put("MALE", 10.0);
            Map<String, Long> countsMap = new HashMap<>();
            countsMap.put("MALE", 2L);
            SumAndCountStatisticsData<Double> sumAndCountStatisticsData = new SumAndCountStatisticsData<>(sumsMap, countsMap);

            // when
            sumAndCountStatisticsData = zeroPaddingToSatisfactionAmountCountsAndSums(sumAndCountStatisticsData, getGenderKeys());

            // then
            List<String> keys = getGenderKeys();
            assertThat(sumAndCountStatisticsData.countData().size()).isEqualTo(keys.size());
            assertThat(sumAndCountStatisticsData.sumData().size()).isEqualTo(keys.size());

            for (var key : keys) {
                if (key.equals("MALE")) {
                    assertThat(sumAndCountStatisticsData.sumData().get(key)).isEqualTo(10.0);
                    assertThat(sumAndCountStatisticsData.countData().get(key)).isEqualTo(2L);
                } else {
                    assertThat(sumAndCountStatisticsData.sumData().get(key)).isEqualTo(0.0);
                    assertThat(sumAndCountStatisticsData.countData().get(key)).isEqualTo(0L);
                }
            }
        }

        @Test
        void 유효하지_않은_키가_있다면_실패한다() throws Exception {
            // given
            Map<String, Double> sumsMap = new HashMap<>();
            sumsMap.put("MALEE", 10.0);
            Map<String, Long> countsMap = new HashMap<>();
            countsMap.put("MALE", 2L);
            SumAndCountStatisticsData<Double> sumAndCountStatisticsData = new SumAndCountStatisticsData<>(sumsMap, countsMap);

            // when // then
            assertThatThrownBy(() -> zeroPaddingToSatisfactionAmountCountsAndSums(sumAndCountStatisticsData, getGenderKeys()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}