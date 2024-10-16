package com.example.spinlog.utils;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.dto.cache.CountsAndSums;
import com.example.spinlog.statistics.utils.StatisticsZeroPaddingUtils;
import com.example.spinlog.user.entity.Gender;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.*;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StatisticsZeroPaddingUtilsTest {
    @Nested
    class zeroPaddingToGenderEmotionAmountCountsAndSums {
        @Test
        void GenderEmotionAmountCountsAndSums에_대해_zero_padding을_수행한다() throws Exception {
            // given
            Map<String, Object> sumsMap = new HashMap<>();
            sumsMap.put("MALE::SAD", 10L);
            Map<String, Object> countsMap = new HashMap<>();
            countsMap.put("MALE::SAD", 1L);
            CountsAndSums countsAndSums = new CountsAndSums(sumsMap, countsMap);

            // when
            countsAndSums = StatisticsZeroPaddingUtils
                    .zeroPaddingToGenderEmotionAmountCountsAndSums(countsAndSums);

            // then
            List<String> genderEmotionKeys = getGenderEmotionKeys();
            assertThat(countsAndSums.countsMap().size()).isEqualTo(genderEmotionKeys.size());
            assertThat(countsAndSums.sumsMap().size()).isEqualTo(genderEmotionKeys.size());

            for (var key : genderEmotionKeys) {
                if (key.equals("MALE::SAD")) {
                    assertThat(countsAndSums.sumsMap().get(key)).isEqualTo(10L);
                    assertThat(countsAndSums.countsMap().get(key)).isEqualTo(1L);
                } else {
                    assertThat(countsAndSums.sumsMap().get(key)).isEqualTo(0L);
                    assertThat(countsAndSums.countsMap().get(key)).isEqualTo(0L);
                }
            }
        }

        @Test
        void 유효하지_않은_키가_있다면_실패한다() throws Exception {
            // given
            Map<String, Object> sumsMap = new HashMap<>();
            sumsMap.put("MAL::SAD", 10L);
            Map<String, Object> countsMap = new HashMap<>();
            countsMap.put("MALE::SADD", 1L);
            CountsAndSums countsAndSums = new CountsAndSums(sumsMap, countsMap);

            // when // then
            assertThatThrownBy(() -> StatisticsZeroPaddingUtils
                    .zeroPaddingToGenderEmotionAmountCountsAndSums(countsAndSums))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        private List<String> getGenderEmotionKeys() {
            return Arrays.stream(Gender.values())
                    .filter(g -> !g.equals(Gender.NONE))
                    .flatMap(g ->
                            Arrays.stream(Emotion.values())
                                    .map(e -> g + "::" + e))
                    .toList();
        }
    }

    @Nested
    class zeroPaddingToGenderDailyAmountSums {
        @Test
        void GenderDailyAmountSums에_대해_zero_padding을_수행한다() throws Exception {
            // given
            Map<String, Object> sums = new HashMap<>();
            sums.put("MALE::2021-01-01", 10L);
            Period period = new Period(
                    LocalDate.of(2021, 1, 1),
                    LocalDate.of(2021, 1, 3));

            // when
            sums = StatisticsZeroPaddingUtils
                    .zeroPaddingToGenderDailyAmountSums(sums, period);

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
            Map<String, Object> sums = new HashMap<>();
            sums.put("MALE::22021-01-01", 10L);
            Period period = new Period(
                    LocalDate.of(2021, 1, 1),
                    LocalDate.of(2021, 1, 3));

            // when // then
            assertThatThrownBy(() -> StatisticsZeroPaddingUtils
                    .zeroPaddingToGenderDailyAmountSums(sums, period))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        private List<String> getGenderDailyKeys(Period period) {
            LocalDate startDate = period.startDate();
            LocalDate endDate = period.endDate();
            List<Gender> genders = Arrays.stream(Gender.values())
                    .filter(g -> !g.equals(Gender.NONE))
                    .toList();
            List<String> list = new ArrayList<>();
            for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
                list.add("MALE::" + date);
                list.add("FEMALE::" + date);
            }
            return list;
        }

    }

    @Nested
    class zeroPaddingToGenderSatisfactionAmountCountsAndSums {
        @Test
        void GenderSatisfactionAmountCountsAndSums에_대해_zero_padding을_수행한다() throws Exception {
            // given
            Map<String, Object> sumsMap = new HashMap<>();
            sumsMap.put("MALE", 10.0);
            Map<String, Object> countsMap = new HashMap<>();
            countsMap.put("MALE", 2L);
            CountsAndSums countsAndSums = new CountsAndSums(sumsMap, countsMap);

            // when
            countsAndSums = StatisticsZeroPaddingUtils
                    .zeroPaddingToGenderSatisfactionAmountCountsAndSums(countsAndSums);

            // then
            List<String> keys = getGenderKeys();
            assertThat(countsAndSums.countsMap().size()).isEqualTo(keys.size());
            assertThat(countsAndSums.sumsMap().size()).isEqualTo(keys.size());

            for (var key : keys) {
                if (key.equals("MALE")) {
                    assertThat(countsAndSums.sumsMap().get(key)).isEqualTo(10.0);
                    assertThat(countsAndSums.countsMap().get(key)).isEqualTo(2L);
                } else {
                    assertThat(countsAndSums.sumsMap().get(key)).isEqualTo(0.0);
                    assertThat(countsAndSums.countsMap().get(key)).isEqualTo(0L);
                }
            }
        }

        @Test
        void 유효하지_않은_키가_있다면_실패한다() throws Exception {
            // given
            Map<String, Object> sumsMap = new HashMap<>();
            sumsMap.put("MALEE", 10.0);
            Map<String, Object> countsMap = new HashMap<>();
            countsMap.put("MALE", 2L);
            CountsAndSums countsAndSums = new CountsAndSums(sumsMap, countsMap);

            // when // then
            assertThatThrownBy(() -> StatisticsZeroPaddingUtils
                    .zeroPaddingToGenderSatisfactionAmountCountsAndSums(countsAndSums))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        private List<String> getGenderKeys() {
            List<String> keys = Arrays.stream(Gender.values())
                    .filter(g -> !g.equals(Gender.NONE))
                    .map(Gender::name)
                    .toList();
            return keys;
        }
    }
}