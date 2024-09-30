package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.event.UserUpdatedEvent;
import com.example.spinlog.utils.StatisticsCacheUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;

import static com.example.spinlog.article.entity.RegisterType.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.StatisticsCacheUtils.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticsCacheSynchronizerAfterUserWrite {
    private final GenderStatisticsCacheWriteService genderStatisticsCacheWriteService;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;


    @TransactionalEventListener
    public void updateStatisticsCacheFromUpdatedUser(UserUpdatedEvent event) {
        User originalUser = event.getOriginalUser();
        User updatedUser = event.getUpdatedUser();
        Gender originalGender = originalUser.getGender();
        Gender updatedGender = updatedUser.getGender();

        if(isGenderChanged(originalUser, updatedUser)) {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(PERIOD_CRITERIA);
            AllStatisticsMap statisticsAllData = genderStatisticsRepositoryFetchService
                    .getGenderStatisticsAllDataByGender(updatedUser.getGender(), startDate, endDate);

            genderStatisticsCacheWriteService.decrementAllData(statisticsAllData);
            genderStatisticsCacheWriteService.incrementAllData(statisticsAllData);

        }

        // todo mbti cache 추가 시 추가
    }

    private boolean isGenderChanged(User originalUser, User updatedUser) {
        return originalUser.getGender() != updatedUser.getGender();
    }
}
