package com.example.spinlog.statistics.scheduled;

import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.caching.GenderStatisticsCacheWriteService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.user.entity.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;

import static com.example.spinlog.article.entity.RegisterType.SAVE;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.GENDER_DAILY_AMOUNT_SUM_KEY_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheRefreshScheduledService {
    private final HashCacheService hashCacheService;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final GenderStatisticsCacheWriteService genderStatisticsCacheWriteService;
    private final StatisticsPeriodManager statisticsPeriodManager;

    // todo prometheus & grafana로 성공 여부 확인
    // todo read 문제는 없음, but article write 시에 Race condition 발생 가능성 있음 -> 캐시 업데이트 할때 lock 걸어야 함
    //  -> PERIOD CRITERIA을 별도의 클래스로 관리하여 lock 걸어야 함
    @Scheduled(cron = "0 0 4 * * *")
    public void refreshGenderStatisticsCache() {
        log.info("Start refreshing Caching.");

        Period period = statisticsPeriodManager.getStatisticsPeriod();
        LocalDate todayStartDate = period.endDate();
        LocalDate todayEndDate = todayStartDate.plusDays(1);
        log.info("newData's startDate: {}, endDate: {}", todayStartDate, todayEndDate);

        AllStatisticsMap newStatisticsData = genderStatisticsRepositoryFetchService
                .getGenderStatisticsAllData(todayStartDate, todayEndDate);
        log.info("\nnewStatisticsData: {}\n", newStatisticsData);

        LocalDate oldStartDate = period.startDate();
        LocalDate oldEndDate = oldStartDate.plusDays(1);
        log.info("expiringData's startDate: {}, endDate: {}", oldStartDate, oldEndDate);

        AllStatisticsMap expiringStatisticsData = genderStatisticsRepositoryFetchService
                .getGenderStatisticsAllData(oldStartDate, oldEndDate);
        log.info("\nexpiringStatisticsData: {}\n", expiringStatisticsData);

        try {
            // todo lock
            decrementOldCacheData(expiringStatisticsData);
            incrementNewCacheData(newStatisticsData);
            deleteExpiringDateCache(oldStartDate);
            zeroPaddingNewDateCache(todayStartDate);
            // todo unlock
        } catch (Exception e) {
            log.error("Error occurred while updating cache data.", e);
        } finally {
            log.info("Finish refreshing Caching.");
            statisticsPeriodManager.updateStatisticsPeriod();
        }
    }

    private void incrementNewCacheData(AllStatisticsMap newStatisticsData) {
        log.info("try to increase all data");
        genderStatisticsCacheWriteService.incrementAllData(newStatisticsData);
    }

    private void decrementOldCacheData(AllStatisticsMap expiringStatisticsData) {
        log.info("try to decrease all data");
        genderStatisticsCacheWriteService.decrementAllData(expiringStatisticsData);
    }

    private void zeroPaddingNewDateCache(LocalDate todayStartDate) {
        log.info("try to zero padding new date cache");
        Arrays.stream(Gender.values()).filter(g -> !g.equals(Gender.NONE))
                .map(g -> g + "::" + todayStartDate)
                .forEach(k -> {
                    if(hashCacheService.getDataFromHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), k) == null) {
                        hashCacheService.putDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), k, 0L);
                    }
                    if(hashCacheService.getDataFromHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), k) == null) {
                        hashCacheService.putDataInHash(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), k, 0L);
                    }
                });
    }

    private void deleteExpiringDateCache(LocalDate oldStartDate) {
        log.info("try to delete expiring date cache");
        Arrays.stream(Gender.values()).filter(g -> !g.equals(Gender.NONE))
                .map(g -> g + "::" + oldStartDate)
                .forEach(k -> {
                    hashCacheService.deleteHashKey(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND), k);
                    hashCacheService.deleteHashKey(GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SAVE), k);
                });
    }
}
