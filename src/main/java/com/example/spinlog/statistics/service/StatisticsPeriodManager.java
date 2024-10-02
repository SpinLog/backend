package com.example.spinlog.statistics.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

        // todo 0시 ~ 4시 안맞음 - 일단은 이 시간을 제외한 시간대에 실행되도록 설정
    }

    public Period getStatisticsPeriod() {
        return new Period(startDate, endDate);
    }

    // todo GenderStatsticsCacheScheduledService에서만 사용하도록 변경
    public void updateStatisticsPeriod() {
        LocalDate endDate = LocalDate.now(clock);
        this.startDate = endDate.minusDays(PERIOD_CRITERIA);
        this.endDate = endDate;
    }

    public record Period(LocalDate startDate, LocalDate endDate) { }
}
