package com.example.spinlog.statistics.scheduled.startup;

import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GenderStatisticsCacheStartupServiceSetPeriodTest {
    GenderStatisticsRepository genderStatisticsRepository = mock(GenderStatisticsRepository.class);
    GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService =
            new GenderStatisticsRepositoryFetchService(genderStatisticsRepository);
    HashCacheService hashCacheService = mock(HashCacheService.class);

    @ParameterizedTest
    @ValueSource(strings = {"00:00:00", "03:59:59"})
    void 오전_4시_이전에_실행되면_어제를_기준으로_Statistics_Period가_세팅된다(String localTime) throws Exception {
        // given
        Clock clock = getFixedClock(localTime);
        StatisticsPeriodManager statisticsPeriodManager = new StatisticsPeriodManager(clock);

        GenderStatisticsCacheStartupService genderStatisticsStartupService =
                new GenderStatisticsCacheStartupService(
                        hashCacheService,
                        genderStatisticsRepositoryFetchService,
                        statisticsPeriodManager);

        // when
        genderStatisticsStartupService.initGenderStatisticsCache();

        // then
        StatisticsPeriodManager.Period period = statisticsPeriodManager.getStatisticsPeriod();

        LocalDate yesterday = LocalDate.now().minusDays(1);
        assertThat(period.startDate()).isEqualTo(yesterday.minusDays(30));
        assertThat(period.endDate()).isEqualTo(yesterday);
    }

    @ParameterizedTest
    @ValueSource(strings = {"04:00:00", "23:59:59"})
    void 오전_4시_이후에_실행되면_오늘을_기준으로_Statistics_Period가_세팅된다(String localTime) throws Exception {
        // given
        Clock clock = getFixedClock(localTime);
        StatisticsPeriodManager statisticsPeriodManager = new StatisticsPeriodManager(clock);

        GenderStatisticsCacheStartupService genderStatisticsStartupService =
                new GenderStatisticsCacheStartupService(
                        hashCacheService,
                        genderStatisticsRepositoryFetchService,
                        statisticsPeriodManager);

        // when
        genderStatisticsStartupService.initGenderStatisticsCache();

        // then
        StatisticsPeriodManager.Period period = statisticsPeriodManager.getStatisticsPeriod();

        LocalDate today = LocalDate.now();
        assertThat(period.startDate()).isEqualTo(today.minusDays(30));
        assertThat(period.endDate()).isEqualTo(today);
    }

    private static Clock getFixedClock(String instant) {
        String now = LocalDate.now().toString();
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(instant));
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return Clock.fixed(
                zonedDateTime.toInstant(),
                ZoneId.systemDefault());
    }

}