package com.example.spinlog.utils;

import com.example.spinlog.statistics.exception.InvalidCacheException;
import com.example.spinlog.statistics.dto.repository.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.repository.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.utils.StatisticsCacheUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.convertToGenderSatisfactionAverageDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StatisticsCacheUtilsTest {
    @Nested
    class convertToGenderEmotionAmountAverageDto {
        @Test
        void 성별_감정별_CountsAndSums_객체를_받아서_GenderEmotionAmountAverageDto의_리스트를_반환한다() throws Exception {
            // given
            SumAndCountStatisticsData sumAndCountStatisticsData = new SumAndCountStatisticsData(
                    Map.of(
                            "MALE::PROUD", 1000L,
                            "MALE::SAD", 2000L,
                            "FEMALE::PROUD", 3000L,
                            "FEMALE::SAD", 4000L),
                    Map.of(
                            "MALE::PROUD", 5L,
                            "MALE::SAD", 5L,
                            "FEMALE::PROUD", 5L,
                            "FEMALE::SAD", 5L));

            // when
            List<GenderEmotionAmountAverageDto> dtos =
                    StatisticsCacheUtils.convertToGenderEmotionAmountAverageDto(sumAndCountStatisticsData);

            // then
            for(GenderEmotionAmountAverageDto dto : dtos) {
                switch(dto.getGender() + "::" + dto.getEmotion()) {
                    case "MALE::HAPPY":
                        assertThat(dto.getAmountAverage()).isEqualTo(200L);
                        break;
                    case "MALE::SAD":
                        assertThat(dto.getAmountAverage()).isEqualTo(400L);
                        break;
                    case "FEMALE::HAPPY":
                        assertThat(dto.getAmountAverage()).isEqualTo(600L);
                        break;
                    case "FEMALE::SAD":
                        assertThat(dto.getAmountAverage()).isEqualTo(800L);
                        break;
                }
            }
        }

        @Test
        void sumsMap이_null이라면_실패한다() throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderEmotionAmountAverageDto(new SumAndCountStatisticsData(null, Map.of())))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        void countsMap이_null이라면_실패한다() throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderEmotionAmountAverageDto(new SumAndCountStatisticsData(Map.of(), null)))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        void sumsMap과_countsMap의_개수가_맞지_않는다면_실패한다() throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderEmotionAmountAverageDto(
                    new SumAndCountStatisticsData(
                            Map.of("key1", 1000L),
                            Map.of("key1", 5L, "key2", 10L))))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        void sumsMap의_key와_countsMap의_key가_맞지_않는다면_실패한다() throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderEmotionAmountAverageDto(
                    new SumAndCountStatisticsData(
                            Map.of("key1", 1000L, "key2", 2000L),
                            Map.of("key1", 5L, "key3", 10L))))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"string", "1.0f", "1.0"})
        void sumsMap의_value가_long_타입이_아니라면_실패한다(String value) throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderEmotionAmountAverageDto(
                    new SumAndCountStatisticsData(
                            Map.of("key1", value),
                            Map.of("key1", 5L))))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"string", "1.0f", "1.0"})
        void countsMap의_value가_long_타입이_아니라면_실패한다(String value) throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderEmotionAmountAverageDto(
                    new SumAndCountStatisticsData(
                            Map.of("key1", 10000L),
                            Map.of("key1", value))))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Stiring1", "InvalidGender::SAD", "MALE::InvalidEmotion"})
        @DisplayName("key 형식이 {Gender}::{Emotion}이 아니라면 실패한다")
        void test_map_key(String key) throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderEmotionAmountAverageDto(
                    new SumAndCountStatisticsData(
                            Map.of(key, 10000L),
                            Map.of(key, 5L))))
                    .isInstanceOf(InvalidCacheException.class);
        }
    }

    @Nested
    class convertToGenderDailyAmountSumDto {
        @Test
        void 성별_일별_sumsMap을_받아서_GenderDailyAmountSumDto를_반환한다() throws Exception {
            // given
            Map<String, Object> sumsMap = Map.of(
                    "MALE::2024-07-01", 1000L,
                    "MALE::2024-07-02", 2000L,
                    "FEMALE::2024-07-01", 3000L,
                    "FEMALE::2024-07-02", 4000L);

            // when
            List<GenderDailyAmountSumDto> dtos =
                    StatisticsCacheUtils.convertToGenderDailyAmountSumDto(sumsMap);

            // then
            for(GenderDailyAmountSumDto dto : dtos) {
                switch (dto.getGender() + "::" + dto.getLocalDate()) {
                    case "MALE::2024-07-01":
                        assertThat(dto.getAmountSum()).isEqualTo(1000L);
                        break;
                    case "MALE::2024-07-02":
                        assertThat(dto.getAmountSum()).isEqualTo(2000L);
                        break;
                    case "FEMALE::2024-07-01":
                        assertThat(dto.getAmountSum()).isEqualTo(3000L);
                        break;
                    case "FEMALE::2024-07-02":
                        assertThat(dto.getAmountSum()).isEqualTo(4000L);
                        break;
                }
            }
        }

        @Test
        void sumsMap이_null이면_실패한다() throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderDailyAmountSumDto(
                    null))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Stiring1", "InvalidGender::2024-07-11", "MALE::InvalidDate"})
        @DisplayName("key 형식이 {Gender}::{Date}가 아니라면 실패한다")
        void sumsMap_key_test(String key) throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderDailyAmountSumDto(
                    Map.of(key, 1000L)))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"string", "1.0f", "1.0"})
        void sumsMap의_value가_long_타입이_아니라면_실패한다(String value) throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderDailyAmountSumDto(
                    Map.of("key1", value)))
                    .isInstanceOf(InvalidCacheException.class);
        }
    }

    @Nested
    class convertToGenderSatisfactionAverageDto {
        @Test
        void 성별_Satisfaction에_대한_CountsAndSums를_받아서_GenderSatisfactionAverageDto의_리스트를_반환한다() throws Exception {
            // given
            SumAndCountStatisticsData sumAndCountStatisticsData = new SumAndCountStatisticsData(
                    Map.of(
                            "MALE", 34.0,
                            "FEMALE", 78.0),
                    Map.of(
                            "MALE", 10L,
                            "FEMALE", 20L));

            // when
            List<GenderSatisfactionAverageDto> dtos = convertToGenderSatisfactionAverageDto(sumAndCountStatisticsData);

            // then
            for(GenderSatisfactionAverageDto dto : dtos) {
                switch(dto.getGender()) {
                    case MALE:
                        assertThat(dto.getSatisfactionAverage()).isEqualTo(34.0f/10);
                        break;
                    case FEMALE:
                        assertThat(dto.getSatisfactionAverage()).isEqualTo(78.0f/20);
                        break;
                }
            }
        }

        @Test
        void sumsMap이_null이라면_실패한다() throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderSatisfactionAverageDto(
                    new SumAndCountStatisticsData(
                            null,
                            Map.of())))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        void countsMap이_null이라면_실패한다() throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderSatisfactionAverageDto(
                    new SumAndCountStatisticsData(
                            Map.of(),
                            null)))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        void sumsMap과_countsMap의_개수가_맞지_않는다면_실패한다() throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderSatisfactionAverageDto(
                    new SumAndCountStatisticsData(
                            Map.of("key1", 10.0),
                            Map.of("key1", 5L, "key2", 10L))))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        void sumsMap의_key와_countsMap의_key가_맞지_않는다면_실패한다() throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderSatisfactionAverageDto(
                    new SumAndCountStatisticsData(
                            Map.of("key1", 12.0, "key2", 10.0),
                            Map.of("key1", 5L, "key3", 10L))))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"string", "1000"})
        void sumsMap의_value가_double_타입이_아니라면_실패한다(String value) throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderSatisfactionAverageDto(
                    new SumAndCountStatisticsData(
                            Map.of("key1", value),
                            Map.of("key1", 5L))))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"string", "1.0f", "1.0"})
        void countsMap의_value가_long_타입이_아니라면_실패한다(String value) throws Exception {
            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderSatisfactionAverageDto(
                    new SumAndCountStatisticsData(
                            Map.of("key1", 10000L),
                            Map.of("key1", value))))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        @DisplayName("key 형식이 {Gender}이 아니라면 실패한다")
        void test_map_key() throws Exception {
            // given
            String key = "InvalidGender";

            // when // then
            assertThatThrownBy(() -> StatisticsCacheUtils.convertToGenderSatisfactionAverageDto(
                    new SumAndCountStatisticsData(
                            Map.of(key, 10000L),
                            Map.of(key, 5L))))
                    .isInstanceOf(InvalidCacheException.class);
        }
    }
}