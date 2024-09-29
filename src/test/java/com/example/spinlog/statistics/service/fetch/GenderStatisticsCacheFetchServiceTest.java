package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.exception.InvalidCacheException;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.CacheKeyNameUtils.getGenderEmotionStatisticsAmountSumKeyName;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsCacheFetchServiceTest {
    HashCacheService hashCacheService = mock(HashCacheService.class);
    GenderStatisticsCacheFetchService genderStatisticsCacheFetchService =
            new GenderStatisticsCacheFetchService(hashCacheService);

    @Nested
    class getAmountAveragesEachGenderAndEmotionLast30Days {
        @Test
        void CacheService로부터_합과_개수를_조회한_후_평균을_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountSumKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::PROUD", 1000L,
                            "MALE::SAD", 2000L,
                            "FEMALE::PROUD", 3000L,
                            "FEMALE::SAD", 4000L));
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountCountKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::PROUD", 5L,
                            "MALE::SAD", 5L,
                            "FEMALE::PROUD", 5L,
                            "FEMALE::SAD", 5L));

            // when
            List<GenderEmotionAmountAverageDto> results = genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(registerType);

            // then
            for(GenderEmotionAmountAverageDto result : results) {
                switch(result.getGender() + "::" + result.getEmotion()) {
                    case "MALE::HAPPY":
                        assertThat(result.getAmountAverage()).isEqualTo(200L);
                        break;
                    case "MALE::SAD":
                        assertThat(result.getAmountAverage()).isEqualTo(400L);
                        break;
                    case "FEMALE::HAPPY":
                        assertThat(result.getAmountAverage()).isEqualTo(600L);
                        break;
                    case "FEMALE::SAD":
                        assertThat(result.getAmountAverage()).isEqualTo(800L);
                        break;
                }
            }
        }
        
        @Test
        void sumsMap이_null이라면_실패한다() throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SPEND))))
                    .thenReturn(null);
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountCountKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of());
            
            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        void countsMap이_null이라면_실패한다() throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of());
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountCountKeyName(RegisterType.SPEND))))
                    .thenReturn(null);

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        void sumsMap과_countsMap의_개수가_맞지_않는다면_실패한다() throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", 1000L));
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountCountKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", 5L, "key2", 10L));

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }
        
        @Test
        void sumsMap의_key와_countsMap의_key가_맞지_않는다면_실패한다() throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", 1000L, "key2", 2000L));
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountCountKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", 5L, "key3", 10L));

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"string", "1.0f", "1.0"})
        void sumsMap의_value가_long_타입이_아니라면_실패한다(String value) throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", value));
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountCountKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", 5L));

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"string", "1.0f", "1.0"})
        void countsMap의_value가_long_타입이_아니라면_실패한다(String value) throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", 10000L));
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountCountKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", value));

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"Stiring1", "InvalidGender::SAD", "MALE::InvalidEmotion"})
        @DisplayName("key 형식이 {Gender}::{Emotion}이 아니라면 실패한다")
        void test_map_key(String key) throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of(key, 10000L));
            when(hashCacheService.getHashEntries(
                    eq(getGenderEmotionStatisticsAmountCountKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of(key, 5L));

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }
    }

    @Nested
    class getAmountSumsEachGenderAndDayLast30Days{
        @Test
        void CacheService로부터_데이터를_조회한_후_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(hashCacheService.getHashEntries(
                    eq(getGenderDailyStatisticsAmountSumKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::2024-07-01", 1000L,
                            "MALE::2024-07-02", 2000L,
                            "FEMALE::2024-07-01", 3000L,
                            "FEMALE::2024-07-02", 4000L));
            
            // when
            List<GenderDailyAmountSumDto> results = genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(registerType);

            // then
            for(GenderDailyAmountSumDto result : results) {
                switch (result.getGender() + "::" + result.getLocalDate()) {
                    case "MALE::2024-07-01":
                        assertThat(result.getAmountSum()).isEqualTo(1000L);
                        break;
                    case "MALE::2024-07-02":
                        assertThat(result.getAmountSum()).isEqualTo(2000L);
                        break;
                    case "FEMALE::2024-07-01":
                        assertThat(result.getAmountSum()).isEqualTo(3000L);
                        break;
                    case "FEMALE::2024-07-02":
                        assertThat(result.getAmountSum()).isEqualTo(4000L);
                        break;
                }
            }
        }

        @Test
        void sumsMap이_null이면_실패한다() throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderDailyStatisticsAmountSumKeyName(RegisterType.SPEND))))
                    .thenReturn(null);

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"Stiring1", "InvalidGender::2024-07-11", "MALE::InvalidDate"})
        @DisplayName("key 형식이 {Gender}::{Date}가 아니라면 실패한다")
        void sumsMap_key_test(String key) throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderDailyStatisticsAmountSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of(key, 1000L));

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"string", "1.0f", "1.0"})
        void sumsMap의_value가_long_타입이_아니라면_실패한다(String value) throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderDailyStatisticsAmountSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", value));

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getAmountSumsEachGenderAndDay(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }
    }

    @Nested
    class getSatisfactionAveragesEachGenderLast30Days{
        @Test
        void CacheService로부터_합과_개수를_조회한_후_평균을_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionSumKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::PROUD", 12.0,
                            "MALE::SAD", 34.0,
                            "FEMALE::PROUD", 56.0,
                            "FEMALE::SAD", 78.0));
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionCountKeyName(registerType))))
                    .thenReturn(Map.of(
                            "MALE::PROUD", 5L,
                            "MALE::SAD", 10L,
                            "FEMALE::PROUD", 12L,
                            "FEMALE::SAD", 20L));

            // when
            List<GenderEmotionAmountAverageDto> results = genderStatisticsCacheFetchService.getAmountAveragesEachGenderAndEmotion(registerType);

            // then
            for(GenderEmotionAmountAverageDto result : results) {
                switch(result.getGender() + "::" + result.getEmotion()) {
                    case "MALE::HAPPY":
                        assertThat(result.getAmountAverage()).isEqualTo(12.0/5);
                        break;
                    case "MALE::SAD":
                        assertThat(result.getAmountAverage()).isEqualTo(34.0/10);
                        break;
                    case "FEMALE::HAPPY":
                        assertThat(result.getAmountAverage()).isEqualTo(56.0/12);
                        break;
                    case "FEMALE::SAD":
                        assertThat(result.getAmountAverage()).isEqualTo(78.0/20);
                        break;
                }
            }
        }

        @Test
        void sumsMap이_null이라면_실패한다() throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionSumKeyName(RegisterType.SPEND))))
                    .thenReturn(null);
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionCountKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of());

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        void countsMap이_null이라면_실패한다() throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of());
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionCountKeyName(RegisterType.SPEND))))
                    .thenReturn(null);

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        void sumsMap과_countsMap의_개수가_맞지_않는다면_실패한다() throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", 10.0));
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionCountKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", 5L, "key2", 10L));

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        void sumsMap의_key와_countsMap의_key가_맞지_않는다면_실패한다() throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", 12.0, "key2", 10.0));
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionCountKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", 5L, "key3", 10L));

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"string", "1000"})
        void sumsMap의_value가_double_타입이_아니라면_실패한다(String value) throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", value));
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionCountKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", 5L));

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"string", "1.0f", "1.0"})
        void countsMap의_value가_long_타입이_아니라면_실패한다(String value) throws Exception {
            // given
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", 10000L));
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionCountKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of("key1", value));

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }

        @Test
        @DisplayName("key 형식이 {Gender}이 아니라면 실패한다")
        void test_map_key() throws Exception {
            // given
            String key = "InvalidGender";
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionSumKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of(key, 10000L));
            when(hashCacheService.getHashEntries(
                    eq(getGenderStatisticsSatisfactionCountKeyName(RegisterType.SPEND))))
                    .thenReturn(Map.of(key, 5L));

            // when // then
            assertThatThrownBy(() -> genderStatisticsCacheFetchService.getSatisfactionAveragesEachGender(RegisterType.SPEND))
                    .isInstanceOf(InvalidCacheException.class);
        }
    }
}