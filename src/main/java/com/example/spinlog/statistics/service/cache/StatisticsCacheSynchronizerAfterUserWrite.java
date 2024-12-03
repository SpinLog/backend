package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.statistics.dto.cache.AllStatisticsCacheData;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.dto.repository.AllGenderStatisticsRepositoryData;
import com.example.spinlog.statistics.dto.repository.AllStatisticsRepositoryData;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.service.fetch.MBTIStatisticsRepositoryFetchService;
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
    private final MBTIStatisticsCacheWriteService mbtiStatisticsCacheWriteService;
    private final MBTIStatisticsRepositoryFetchService mbtiStatisticsRepositoryFetchService;
    private final StatisticsPeriodManager statisticsPeriodManager;


    @TransactionalEventListener
    public void updateStatisticsCacheFromUpdatedUser(UserUpdatedEvent event) {
        User originalUser = event.getOriginalUser();
        User updatedUser = event.getUpdatedUser();

        Period period = statisticsPeriodManager.getStatisticsPeriod();
        LocalDate endDate = period.endDate();
        LocalDate startDate = period.startDate();

        if(isGenderChanged(originalUser, updatedUser)) {
            AllStatisticsRepositoryData repositoryResult = genderStatisticsRepositoryFetchService
                    .getGenderStatisticsAllDataByUserId(updatedUser.getId(), startDate, endDate);

            updateGenderStatisticsCacheForPreviousGender(repositoryResult, originalUser);
            updateGenderStatisticsCacheForChangedGender(repositoryResult, updatedUser);
        }

        if(isMBTIChanged(originalUser, updatedUser)) {
            AllStatisticsRepositoryData repositoryResult = mbtiStatisticsRepositoryFetchService
                    .getAllMBTIStatisticsRepositoryDataByUserId(updatedUser.getId(), startDate, endDate);

            updateMBTIStatisticsCacheForPreviousMBTI(repositoryResult, originalUser);
            updateMBTIStatisticsCacheForChangedMBTI(repositoryResult, updatedUser);
        }
    }

    private boolean isMBTIChanged(User originalUser, User updatedUser) {
        return !originalUser.getMbti().equals(updatedUser.getMbti());
    }

    private boolean isGenderChanged(User originalUser, User updatedUser) {
        return originalUser.getGender() != updatedUser.getGender();
    }

    private void updateGenderStatisticsCacheForPreviousGender(AllStatisticsRepositoryData repositoryResult, User originalUser) {
        AllStatisticsCacheData genderReversedData = repositoryResult.toCacheDate(originalUser.getGender());
        genderStatisticsCacheWriteService.decrementAllData(genderReversedData);
    }

    private void updateGenderStatisticsCacheForChangedGender(AllStatisticsRepositoryData repositoryResult, User updatedUser) {
        AllStatisticsCacheData statisticsAllData = repositoryResult.toCacheDate(updatedUser.getGender());
        genderStatisticsCacheWriteService.incrementAllData(statisticsAllData);
    }

    private void updateMBTIStatisticsCacheForPreviousMBTI(AllStatisticsRepositoryData repositoryResult, User originalUser) {
        for(char mbtiFactor: originalUser.getMbti().toString().toCharArray()){
            AllStatisticsCacheData mbtiReversedData = repositoryResult.toCacheDate(MBTIFactor.valueOf(String.valueOf(mbtiFactor)));
            mbtiStatisticsCacheWriteService.decrementAllData(mbtiReversedData);
        }
    }

    private void updateMBTIStatisticsCacheForChangedMBTI(AllStatisticsRepositoryData repositoryResult, User updatedUser) {
        for(char mbtiFactor: updatedUser.getMbti().toString().toCharArray()){
            AllStatisticsCacheData mbtiReversedData = repositoryResult.toCacheDate(MBTIFactor.valueOf(String.valueOf(mbtiFactor)));
            mbtiStatisticsCacheWriteService.incrementAllData(mbtiReversedData);
        }
    }
}
