package com.example.spinlog.statistics.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Slf4j
public class StatisticsPeriodManager {
    private static final int PERIOD_CRITERIA = 30;
    private LocalDate startDate;
    private LocalDate endDate;
    private final Clock clock;

    public StatisticsPeriodManager(Clock clock) {
        this.clock = clock;
        LocalDate endDate = LocalDate.now(clock);
        this.startDate = endDate.minusDays(PERIOD_CRITERIA);
        this.endDate = endDate;
    }

    public Period getStatisticsPeriod() {
        return new Period(startDate, endDate);
    }

    /***
     * todo (have to be called only in GenderStatsticsCacheScheduledService)
     * every 4 a.m. update statistics period
     */
    public void updateStatisticsPeriod() {
        log.info("Update statistics period, before startDate: {}, endDate: {}", startDate, endDate);
        LocalDate endDate = LocalDate.now(clock);
        this.startDate = endDate.minusDays(PERIOD_CRITERIA);
        this.endDate = endDate;
        log.info("Statistics period is updated as startDate: {}, endDate: {}", startDate, endDate);
    }

    /***
     * todo (have to be called only in GenderStatsticsCacheStartupService)
     * Immediately after being started spring boot application, update statistics period
     */
    public void setStatisticsPeriodImmediatelyAfterSpringBootIsStarted() {
        LocalTime now = LocalTime.now(clock);
        LocalTime FOUR_AM = LocalTime.of(4, 0);
        if(now.isBefore(FOUR_AM)) {
            log.info("current time: {}, update statistics period as of yesterday", now);
            LocalDate yesterday = LocalDate.now().minusDays(1);
            this.startDate = yesterday.minusDays(PERIOD_CRITERIA);
            this.endDate = yesterday;
        } else {
            log.info("current time: {}, update statistics period as of today", now);
            LocalDate today = LocalDate.now();
            this.startDate = today.minusDays(PERIOD_CRITERIA);
            this.endDate = today;
        }
        log.info("Statistics period is updated as startDate: {}, endDate: {}", startDate, endDate);
    }

    public record Period(LocalDate startDate, LocalDate endDate) { }
}
