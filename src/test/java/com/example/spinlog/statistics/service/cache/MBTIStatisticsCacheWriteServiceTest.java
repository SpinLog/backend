package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.cache.SumAndCountStatisticsData;
import com.example.spinlog.util.MockCacheHashRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static com.example.spinlog.article.entity.RegisterType.SAVE;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
class MBTIStatisticsCacheWriteServiceTest {

    CacheHashRepository cacheHashRepository = new MockCacheHashRepository();

    MBTIStatisticsCacheWriteService targetService =
            new MBTIStatisticsCacheWriteService(cacheHashRepository);

    @Test
    void putAmountCountsAndSumsByMBTIAndEmotion() {
        // given
        SumAndCountStatisticsData<Long> amountSumAndCountStatisticsData = new SumAndCountStatisticsData<>(Map.of("key1", 10L), Map.of("key1", 2L));

        // when
        targetService.putAmountCountsAndSumsByMBTIAndEmotion(amountSumAndCountStatisticsData, SAVE);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key1", 10L));
        entries = cacheHashRepository.getHashEntries(MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key1", 2L));
    }

    @Test
    void putAmountSumsByMBTIAndDate() {
        // given
        Map<String, Long> amountSums = Map.of("key1", 1L);

        // when
        targetService.putAmountSumsByMBTIAndDate(amountSums, SPEND);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SPEND));
        assertThat(entries).isEqualTo(Map.of("key1", 1L));
    }

    @Test
    void putSatisfactionCountsAndSumsByMBTI() {
        // given
        SumAndCountStatisticsData<Double> satisfactionSumAndCountStatisticsData = new SumAndCountStatisticsData<>(Map.of("key1", 5.0), Map.of("key1", 2L));

        // when
        targetService.putSatisfactionCountsAndSumsByMBTI(satisfactionSumAndCountStatisticsData, SAVE);

        // then
        Map<String, Object> entries = cacheHashRepository.getHashEntries(MBTI_SATISFACTION_SUM_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key1", 5.0));
        entries = cacheHashRepository.getHashEntries(MBTI_SATISFACTION_COUNT_KEY_NAME(SAVE));
        assertThat(entries).isEqualTo(Map.of("key1", 2L));
    }
}