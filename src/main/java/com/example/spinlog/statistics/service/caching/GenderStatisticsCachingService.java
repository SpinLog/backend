package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.repository.dto.MemoDto;
import com.example.spinlog.user.entity.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCachingService {
    private final RedisService redisService;

    public List<GenderEmotionAmountAverageDto> getAmountAveragesEachGenderAndEmotionLast30Days(RegisterType registerType) {
        Map<String, Object> hashEntriesSum = redisService.getHashEntries("GenderEmotionStatisticsSum::" + registerType);

        Map<String, Object> hashEntriesCount = redisService.getHashEntries("GenderEmotionStatisticsCount::" + registerType);

        hashEntriesSum.forEach((key, value) -> {
            if (!hashEntriesCount.containsKey(key)) {
                throw new IllegalStateException("Key " + key + " is missing in hashEntriesCount");
            }
        });

        Map<String, Long> genderEmotionAmountAverage = new HashMap<>();
        hashEntriesSum.forEach((k,v) -> {
                    long amount = Long.parseLong(v.toString());
                    long count = Long.parseLong(hashEntriesCount.get(k).toString());
                    long average = Math.round((double) amount / count);
                    genderEmotionAmountAverage.put(k, average);
                });

        return genderEmotionAmountAverage.entrySet().stream()
                .map(e -> {
                    String[] key = e.getKey().split("::");
                    assert key.length == 2;
                    return new GenderEmotionAmountAverageDto(
                            Gender.valueOf(key[0]),
                            Emotion.valueOf(key[1]),
                            Long.parseLong(e.getValue().toString()));
                }).toList();
    }

    public List<GenderDailyAmountSumDto> getAmountSumsEachGenderAndDayLast30Days(RegisterType registerType) {
        Map<String, Object> hashEntriesSum = redisService.getHashEntries("GenderDailyAmountStatisticsSum::" + registerType);

        return hashEntriesSum.entrySet().stream()
                .map(e -> {
                    String[] key = e.getKey().split("::");
                    assert key.length == 2;
                    // todo exception handling
                    LocalDate date = LocalDate.parse(key[1]);
                    return new GenderDailyAmountSumDto(
                            Gender.valueOf(key[0]),
                            date,
                            Long.parseLong(e.getValue().toString()));
                }).toList();
    }

    public List<MemoDto> getAllMemosByGenderLast30Days(RegisterType registerType, Gender gender) {
        return null;
    }

    public List<GenderSatisfactionAverageDto> getSatisfactionAveragesEachGenderLast30Days(RegisterType registerType) {
        Map<String, Object> hashEntriesSum = redisService.getHashEntries("GenderSatisfactionStatisticsSum::" + registerType);

        Map<String, Object> hashEntriesCount = redisService.getHashEntries("GenderSatisfactionStatisticsCount::" + registerType);

        // tood 메서드로 분리
        hashEntriesSum.forEach((key, value) -> {
            if (!hashEntriesCount.containsKey(key)) {
                throw new IllegalStateException("Key " + key + " is missing in hashEntriesCount");
            }
        });

        Map<String, Float> genderSatisfactionAverage = new HashMap<>();
        hashEntriesSum.forEach((k,v) -> {
            long amount = Long.parseLong(v.toString());
            long count = Long.parseLong(hashEntriesCount.get(k).toString());
            float average = (float) amount / (float) count;
            genderSatisfactionAverage.put(k, average);
        });

        return genderSatisfactionAverage.entrySet().stream()
                .map(e -> GenderSatisfactionAverageDto.builder()
                        .gender(Gender.valueOf(e.getKey()))
                        .satisfactionAverage(e.getValue())
                        .build())
                .toList();
    }
}
