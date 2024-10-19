package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.dto.repository.AllGenderStatisticsRepositoryData;
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
            AllGenderStatisticsRepositoryData repositoryResult = genderStatisticsRepositoryFetchService
                    .getGenderStatisticsAllDataByUserId(updatedUser.getId(), startDate, endDate);

            AllStatisticsCacheData statisticsAllData = AllStatisticsCacheData.builder()
                    .emotionAmountSpendSumAndCountStatisticsData(
                            new SumAndCountStatisticsData<>(toGenderEmotionMap(repositoryResult.genderEmotionAmountSpendSums()),
                                    toGenderEmotionMap(repositoryResult.genderEmotionAmountSpendCounts())))
                    .emotionAmountSaveSumAndCountStatisticsData(
                            new SumAndCountStatisticsData<>(toGenderEmotionMap(repositoryResult.genderEmotionAmountSaveSums()),
                                    toGenderEmotionMap(repositoryResult.genderEmotionAmountSaveCounts())))
                    .dailyAmountSpendSums(toGenderDateMap(repositoryResult.genderDailyAmountSpendSums()))
                    .dailyAmountSaveSums(toGenderDateMap(repositoryResult.genderDailyAmountSaveSums()))
                    .satisfactionSpendSumAndCountStatisticsData(
                            new SumAndCountStatisticsData<>(toGenderMap(repositoryResult.genderSatisfactionSpendSums()),
                                    toGenderMap(repositoryResult.genderSatisfactionSpendCounts())))
                    .satisfactionSaveSumAndCountStatisticsData(
                            new SumAndCountStatisticsData<>(toGenderMap(repositoryResult.genderSatisfactionSaveSums()),
                                    toGenderMap(repositoryResult.genderSatisfactionSaveCounts())))
                    .build();
            AllStatisticsCacheData genderReversedData = AllStatisticsCacheData.builder()
                    .emotionAmountSpendSumAndCountStatisticsData(
                            new SumAndCountStatisticsData<>(toReverseGenderEmotionMap(repositoryResult.genderEmotionAmountSpendSums()),
                                    toReverseGenderEmotionMap(repositoryResult.genderEmotionAmountSpendCounts())))
                    .emotionAmountSaveSumAndCountStatisticsData(
                            new SumAndCountStatisticsData<>(toReverseGenderEmotionMap(repositoryResult.genderEmotionAmountSaveSums()),
                                    toReverseGenderEmotionMap(repositoryResult.genderEmotionAmountSaveCounts())))
                    .dailyAmountSpendSums(toReverseGenderDateMap(repositoryResult.genderDailyAmountSpendSums()))
                    .dailyAmountSaveSums(toReverseGenderDateMap(repositoryResult.genderDailyAmountSaveSums()))
                    .satisfactionSpendSumAndCountStatisticsData(
                            new SumAndCountStatisticsData<>(toReverseGenderMap(repositoryResult.genderSatisfactionSpendSums()),
                                    toReverseGenderMap(repositoryResult.genderSatisfactionSpendCounts())))
                    .satisfactionSaveSumAndCountStatisticsData(
                            new SumAndCountStatisticsData<>(toReverseGenderMap(repositoryResult.genderSatisfactionSaveSums()),
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
