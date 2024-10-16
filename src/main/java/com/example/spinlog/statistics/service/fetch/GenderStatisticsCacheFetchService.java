package com.example.spinlog.statistics.service.fetch;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.repository.dto.MemoDto;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService.CountsAndSums;
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

    public CountsAndSums getAmountAveragesEachGenderAndEmotion(RegisterType registerType) {
        Map<String, Object> sumsMap = cacheHashRepository.getHashEntries(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType));
        sumsMap = convertValuesToLong(sumsMap);

        Map<String, Object> countsMap = cacheHashRepository.getHashEntries(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType));
        countsMap = convertValuesToLong(countsMap);

        return new CountsAndSums(sumsMap, countsMap);
    }

    public Map<String, Object> getAmountSumsEachGenderAndDay(RegisterType registerType) {
        Map<String, Object> sumsMap = cacheHashRepository.getHashEntries(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType));
        sumsMap = convertValuesToLong(sumsMap);

        return sumsMap;
    }

    public List<MemoDto> getAllMemosByGender(RegisterType registerType, Gender gender) {
        return null;
    }

    public CountsAndSums getSatisfactionAveragesEachGender(RegisterType registerType) {
        Map<String, Object> sumsMap = cacheHashRepository.getHashEntries(
                GENDER_SATISFACTION_SUM_KEY_NAME(registerType));
        sumsMap = convertValuesToDouble(sumsMap);

        Map<String, Object> countsMap = cacheHashRepository.getHashEntries(
                GENDER_SATISFACTION_COUNT_KEY_NAME(registerType));
        countsMap = convertValuesToLong(countsMap);

        return new CountsAndSums(sumsMap, countsMap);
    }
}
