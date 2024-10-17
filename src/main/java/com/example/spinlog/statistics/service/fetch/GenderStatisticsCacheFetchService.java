package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.statistics.dto.repository.MemoDto;
import com.example.spinlog.user.entity.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.MapCastingUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheFetchService {
    private final CacheHashRepository cacheHashRepository;

    public SumAndCountStatisticsData<Long> getAmountAveragesEachGenderAndEmotion(RegisterType registerType) {
        Map<String, Object> sumData = cacheHashRepository.getHashEntries(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType));
        Map<String, Long> castedSumData = convertValuesToLong(sumData);

        Map<String, Object> countsMap = cacheHashRepository.getHashEntries(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType));
        Map<String, Long> castedCountData = convertValuesToLong(countsMap);

        return new SumAndCountStatisticsData<>(
                castedSumData, castedCountData);
    }

    public Map<String, Long> getAmountSumsEachGenderAndDay(RegisterType registerType) {
        Map<String, Object> sumData = cacheHashRepository.getHashEntries(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType));
        return convertValuesToLong(sumData);
    }

    public List<MemoDto> getAllMemosByGender(RegisterType registerType, Gender gender) {
        return null;
    }

    public SumAndCountStatisticsData<Double> getSatisfactionAveragesEachGender(RegisterType registerType) {
        Map<String, Object> sumData = cacheHashRepository.getHashEntries(
                GENDER_SATISFACTION_SUM_KEY_NAME(registerType));
        Map<String, Double> castedSumData = convertValuesToDouble(sumData);

        Map<String, Object> countData = cacheHashRepository.getHashEntries(
                GENDER_SATISFACTION_COUNT_KEY_NAME(registerType));
        Map<String, Long> castedCountData = convertValuesToLong(countData);

        return new SumAndCountStatisticsData<>(
                castedSumData, castedCountData);
    }
}
