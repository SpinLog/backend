package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.dto.DailyAmountSumDto;
import com.example.spinlog.statistics.dto.EmotionAmountSumAndCountDto;
import com.example.spinlog.statistics.dto.SatisfactionSumAndCountDto;
import com.example.spinlog.statistics.dto.repository.*;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.statistics.service.fetch.GenderStatisticsRepositoryFetchService;
import com.example.spinlog.statistics.service.fetch.MBTIStatisticsRepositoryFetchService;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.event.UserUpdatedEvent;
import com.example.spinlog.util.ArticleFactory;
import com.example.spinlog.util.MockCacheHashRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StatisticsCacheSynchronizerAfterUserWriteTest {
    CacheHashRepository cacheHashRepository = new MockCacheHashRepository();
    StatisticsPeriodManager statisticsPeriodManager = new StatisticsPeriodManager(Clock.systemDefaultZone());
    GenderStatisticsCacheWriteService genderStatisticsCacheWriteService =
            new GenderStatisticsCacheWriteService(cacheHashRepository);
    MBTIStatisticsCacheWriteService mbtiStatisticsCacheWriteService =
            new MBTIStatisticsCacheWriteService(cacheHashRepository);
    GenderStatisticsRepositoryFetchService genderStatisticsRepositoryFetchService =
            mock(GenderStatisticsRepositoryFetchService.class);
    MBTIStatisticsRepositoryFetchService mbtiStatisticsRepositoryFetchService =
            mock(MBTIStatisticsRepositoryFetchService.class);

    StatisticsCacheSynchronizerAfterUserWrite targetService =
            new StatisticsCacheSynchronizerAfterUserWrite(genderStatisticsCacheWriteService,
                genderStatisticsRepositoryFetchService, mbtiStatisticsCacheWriteService,
                mbtiStatisticsRepositoryFetchService,
                statisticsPeriodManager);

    Mbti mbti = Mbti.ISTJ;
    User user= User.builder()
            .email("test@example.com")
            .mbti(mbti)
            .gender(Gender.MALE)
            .authenticationName("testUser")
            .build();
    Article article = ArticleFactory.builder()
            .spendDate(LocalDateTime.now().minusDays(1))
            .emotion(Emotion.PROUD)
            .amount(5)
            .satisfaction(5.0f)
            .build()
            .createArticle();

    @Test
    void 유저의_성별이_변경되면_성별_캐시가_업데이트_된다() throws Exception {
        // given
        Gender newGender = (user.getGender().equals(Gender.MALE)?Gender.FEMALE:Gender.MALE);
        User newUser = User.builder()
                .email(user.getEmail())
                .mbti(user.getMbti())
                .gender(newGender)
                .authenticationName(user.getAuthenticationName())
                .build();
        UserUpdatedEvent event = new UserUpdatedEvent(user, newUser);
        when(genderStatisticsRepositoryFetchService.getGenderStatisticsAllDataByUserId(any(), any(), any()))
                .thenReturn(getAllGenderStatisticsRepositoryData(newGender));

        // when
        targetService.updateStatisticsCacheFromUpdatedUser(event);

        // then
        verify(genderStatisticsRepositoryFetchService).getGenderStatisticsAllDataByUserId(any(), any(), any());

        assertThat(cacheHashRepository.getDataFromHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                user.getGender() + "::" + article.getEmotion())).isEqualTo(-50L);
        assertThat(cacheHashRepository.getDataFromHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND),
                user.getGender() + "::" + article.getEmotion())).isEqualTo(-5L);

        assertThat(cacheHashRepository.getDataFromHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND),
                user.getGender() + "::" + article.getSpendDate().toLocalDate())).isEqualTo(-50L);

        assertThat(cacheHashRepository.getDataFromHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND),
                user.getGender().name())).isEqualTo(-5.0);
        assertThat(cacheHashRepository.getDataFromHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND),
                user.getGender().name())).isEqualTo(-5L);

        assertThat(cacheHashRepository.getDataFromHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                newGender + "::" + article.getEmotion())).isEqualTo(50L);
        assertThat(cacheHashRepository.getDataFromHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND),
                newGender + "::" + article.getEmotion())).isEqualTo(5L);

        assertThat(cacheHashRepository.getDataFromHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(SPEND),
                newGender + "::" + article.getSpendDate().toLocalDate())).isEqualTo(50L);

        assertThat(cacheHashRepository.getDataFromHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(SPEND),
                newGender.name())).isEqualTo(5.0);
        assertThat(cacheHashRepository.getDataFromHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(SPEND),
                newGender.name())).isEqualTo(5L);

    }

    @Test
    void 유저의_MBTI가_변경되면_MBTI_캐시가_업데이트_된다() throws Exception {
        // given
        Mbti newMbti = Mbti.ENFP;
        User newUser = User.builder()
                .email(user.getEmail())
                .mbti(newMbti)
                .gender(user.getGender())
                .authenticationName(user.getAuthenticationName())
                .build();
        UserUpdatedEvent event = new UserUpdatedEvent(user, newUser);
        when(mbtiStatisticsRepositoryFetchService.getAllMBTIStatisticsRepositoryDataByUserId(any(), any(), any()))
                .thenReturn(getAllMBTIStatisticsRepositoryData());

        // when
        targetService.updateStatisticsCacheFromUpdatedUser(event);

        // then
        verify(mbtiStatisticsRepositoryFetchService).getAllMBTIStatisticsRepositoryDataByUserId(any(), any(), any());

        for(char factor: user.getMbti().toString().toCharArray()){
            assertThat(cacheHashRepository.getDataFromHash(
                    MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                    factor + "::" + article.getEmotion())).isEqualTo(-50L);
            assertThat(cacheHashRepository.getDataFromHash(
                    MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND),
                    factor + "::" + article.getEmotion())).isEqualTo(-5L);

            assertThat(cacheHashRepository.getDataFromHash(
                    MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SPEND),
                    factor + "::" + article.getSpendDate().toLocalDate())).isEqualTo(-50L);

            assertThat(cacheHashRepository.getDataFromHash(
                    MBTI_SATISFACTION_SUM_KEY_NAME(SPEND),
                    String.valueOf(factor))).isEqualTo(-5.0);
            assertThat(cacheHashRepository.getDataFromHash(
                    MBTI_SATISFACTION_COUNT_KEY_NAME(SPEND),
                    String.valueOf(factor))).isEqualTo(-5L);
        }

        for(char factor: newMbti.toString().toCharArray()){
            assertThat(cacheHashRepository.getDataFromHash(
                    MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(SPEND),
                    factor + "::" + article.getEmotion())).isEqualTo(50L);
            assertThat(cacheHashRepository.getDataFromHash(
                    MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(SPEND),
                    factor + "::" + article.getEmotion())).isEqualTo(5L);

            assertThat(cacheHashRepository.getDataFromHash(
                    MBTI_DAILY_AMOUNT_SUM_KEY_NAME(SPEND),
                    factor + "::" + article.getSpendDate().toLocalDate())).isEqualTo(50L);

            assertThat(cacheHashRepository.getDataFromHash(
                    MBTI_SATISFACTION_SUM_KEY_NAME(SPEND),
                    String.valueOf(factor))).isEqualTo(5.0);
            assertThat(cacheHashRepository.getDataFromHash(
                    MBTI_SATISFACTION_COUNT_KEY_NAME(SPEND),
                    String.valueOf(factor))).isEqualTo(5L);
        }
    }

    @Test
    void 유저의_성별이_변경되지_않았다면_성별_캐시가_업데이트_되지_않는다() throws Exception {
        // given
        UserUpdatedEvent event = new UserUpdatedEvent(user, user);

        // when
        targetService.updateStatisticsCacheFromUpdatedUser(event);

        // then
        verify(genderStatisticsRepositoryFetchService, never()).getGenderStatisticsAllDataByUserId(any(), any(), any());
    }

    @Test
    void 유저의_MBTI가_변경되지_않았다면_MBTI_캐시가_업데이트_되지_않는다() throws Exception {
        // given
        UserUpdatedEvent event = new UserUpdatedEvent(user, user);

        // when
        targetService.updateStatisticsCacheFromUpdatedUser(event);

        // then
        verify(mbtiStatisticsRepositoryFetchService, never()).getAllMBTIStatisticsRepositoryDataByUserId(any(), any(), any());
    }

    private AllMBTIStatisticsRepositoryData getAllMBTIStatisticsRepositoryData() {
        return AllMBTIStatisticsRepositoryData.builder()
                .emotionAmountSpendSumsAndCounts(List.of(
                        new EmotionAmountSumAndCountDto(Emotion.PROUD, 50L, 5L)))
                .emotionAmountSaveSumsAndCounts(List.of())
                .dailyAmountSpendSums(List.of(
                        new DailyAmountSumDto(article.getSpendDate().toLocalDate(), 50L)))
                .dailyAmountSaveSums(List.of())
                .satisfactionSpendSumsAndCounts(List.of(
                        new SatisfactionSumAndCountDto(5.0, 5L)))
                .satisfactionSaveSumsAndCounts(List.of())
                .build();
    }

    private AllGenderStatisticsRepositoryData getAllGenderStatisticsRepositoryData(Gender newGender) {
        Emotion emotion = article.getEmotion();
        Gender gender = user.getGender();
        return AllGenderStatisticsRepositoryData.builder()
                .genderEmotionAmountSpendSums(List.of(
                        new GenderEmotionAmountAverageDto(newGender, emotion, 50L)))
                .genderEmotionAmountSpendCounts(List.of(
                        new GenderEmotionAmountAverageDto(newGender, emotion, 5L)))
                .genderEmotionAmountSaveSums(List.of())
                .genderEmotionAmountSaveCounts(List.of())
                .genderDailyAmountSpendSums(List.of(
                        new GenderDailyAmountSumDto(newGender, article.getSpendDate().toLocalDate(), 50L)))
                .genderDailyAmountSaveSums(List.of())
                .genderSatisfactionSpendSums(List.of(
                        new GenderDataDto<>(newGender, 5.0)))
                .genderSatisfactionSpendCounts(List.of(
                        new GenderDataDto<>(newGender, 5L)))
                .genderSatisfactionSaveSums(List.of())
                .genderSatisfactionSaveCounts(List.of())
                .build();
    }
}