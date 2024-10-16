package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.statistics.dto.cache.AllStatisticsMap;
import com.example.spinlog.statistics.dto.cache.CountsAndSums;
import com.example.spinlog.statistics.dto.repository.AllStatisticsResult;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.event.UserUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;

import static com.example.spinlog.statistics.service.StatisticsPeriodManager.*;
import static com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.*;
import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticsCacheSynchronizerAfterUserWrite {
    private final GenderStatisticsCacheWriteService genderStatisticsCacheWriteService;
    private final GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService;
    private final StatisticsPeriodManager statisticsPeriodManager;


    @TransactionalEventListener
    public void updateStatisticsCacheFromUpdatedUser(UserUpdatedEvent event) {
        User originalUser = event.getOriginalUser();
        User updatedUser = event.getUpdatedUser();

        if(isGenderChanged(originalUser, updatedUser)) {
            Period period = statisticsPeriodManager.getStatisticsPeriod();
            LocalDate endDate = period.endDate();
            LocalDate startDate = period.startDate();
            AllStatisticsResult repositoryResult = genderStatisticsRepositoryFetchService
                    .getGenderStatisticsAllDataByUserId(updatedUser.getId(), startDate, endDate);

            AllStatisticsMap statisticsAllData = AllStatisticsMap.builder()
                    .genderEmotionAmountSpendCountsAndSums(
                            new CountsAndSums(toGenderEmotionMap(repositoryResult.genderEmotionAmountSpendSums()),
                                    toGenderEmotionMap(repositoryResult.genderEmotionAmountSpendCounts())))
                    .genderEmotionAmountSaveCountsAndSums(
                            new CountsAndSums(toGenderEmotionMap(repositoryResult.genderEmotionAmountSaveSums()),
                                    toGenderEmotionMap(repositoryResult.genderEmotionAmountSaveCounts())))
                    .genderDailyAmountSpendSums(toGenderDateMap(repositoryResult.genderDailyAmountSpendSums()))
                    .genderDailyAmountSaveSums(toGenderDateMap(repositoryResult.genderDailyAmountSaveSums()))
                    .genderSatisfactionSpendCountsAndSums(
                            new CountsAndSums(toGenderMap(repositoryResult.genderSatisfactionSpendSums()),
                                    toGenderMap(repositoryResult.genderSatisfactionSpendCounts())))
                    .genderSatisfactionSaveCountsAndSums(
                            new CountsAndSums(toGenderMap(repositoryResult.genderSatisfactionSaveSums()),
                                    toGenderMap(repositoryResult.genderSatisfactionSaveCounts())))
                    .build();
            AllStatisticsMap genderReversedData = AllStatisticsMap.builder()
                    .genderEmotionAmountSpendCountsAndSums(
                            new CountsAndSums(toReverseGenderEmotionMap(repositoryResult.genderEmotionAmountSpendSums()),
                                    toReverseGenderEmotionMap(repositoryResult.genderEmotionAmountSpendCounts())))
                    .genderEmotionAmountSaveCountsAndSums(
                            new CountsAndSums(toReverseGenderEmotionMap(repositoryResult.genderEmotionAmountSaveSums()),
                                    toReverseGenderEmotionMap(repositoryResult.genderEmotionAmountSaveCounts())))
                    .genderDailyAmountSpendSums(toReverseGenderDateMap(repositoryResult.genderDailyAmountSpendSums()))
                    .genderDailyAmountSaveSums(toReverseGenderDateMap(repositoryResult.genderDailyAmountSaveSums()))
                    .genderSatisfactionSpendCountsAndSums(
                            new CountsAndSums(toReverseGenderMap(repositoryResult.genderSatisfactionSpendSums()),
                                    toReverseGenderMap(repositoryResult.genderSatisfactionSpendCounts())))
                    .genderSatisfactionSaveCountsAndSums(
                            new CountsAndSums(toReverseGenderMap(repositoryResult.genderSatisfactionSaveSums()),
                                    toReverseGenderMap(repositoryResult.genderSatisfactionSaveCounts())))
                    .build();

            genderStatisticsCacheWriteService.decrementAllData(genderReversedData);
            genderStatisticsCacheWriteService.incrementAllData(statisticsAllData);

        }

        // todo mbti cache 추가 시 추가
    }

    private boolean isGenderChanged(User originalUser, User updatedUser) {
        return originalUser.getGender() != updatedUser.getGender();
    }
}
