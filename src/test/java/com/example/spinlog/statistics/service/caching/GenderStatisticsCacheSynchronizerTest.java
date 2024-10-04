package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.event.ArticleCreatedEvent;
import com.example.spinlog.article.event.ArticleDeletedEvent;
import com.example.spinlog.global.cache.HashCacheService;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.util.ArticleFactory;
import com.example.spinlog.util.MockHashCacheService;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.CacheKeyNameUtils.GENDER_SATISFACTION_COUNT_KEY_NAME;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsCacheSynchronizerTest {
    HashCacheService hashCacheService = spy(MockHashCacheService.class);
    StatisticsPeriodManager statisticsPeriodManager = new StatisticsPeriodManager(Clock.systemDefaultZone());
    GenderStatisticsCacheSynchronizer targetService =
            new GenderStatisticsCacheSynchronizer(
                    hashCacheService,
                    statisticsPeriodManager);

    RegisterType registerType = RegisterType.SPEND;
    User user = User.builder()
            .gender(Gender.MALE)
            .authenticationName("test")
            .email("email@email")
            .build();
    Article article = ArticleFactory.builder()
            .spendDate(LocalDateTime.now().minusDays(1))
            .emotion(Emotion.PROUD)
            .amount(5)
            .satisfaction(5.0f)
            .build()
            .createArticle();

    @BeforeEach
    void setUp() {
        hashCacheService.putDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                user.getGender() + "::" + article.getEmotion(), 0L);
        hashCacheService.putDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                user.getGender() + "::" + article.getEmotion(), 0L);

        hashCacheService.putDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                user.getGender() + "::" + article.getSpendDate().toLocalDate(), 0L);

        hashCacheService.putDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                user.getGender().name(), 0.0);
        hashCacheService.putDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                user.getGender().name(), 0L);
    }

    @Nested
    class updateStatisticsCacheFromNewData{
        @Test
        void User의_gender과_Article의_emotion에_해당하는_amount_캐시를_amount만큼_증가시킨다() throws Exception {
            // given
            hashCacheService.putDataInHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1000L);
            hashCacheService.putDataInHash(
                    GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1L);

            // when
            targetService.updateStatisticsCacheFromNewData(new ArticleCreatedEvent(article, user));

            // then
            Object sum = hashCacheService.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            Object count = hashCacheService.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            assertThat(sum).isEqualTo(1000L + article.getAmount());
            assertThat(count).isEqualTo(2L);
        }

        @Test
        void User의_gender와_Article의_spendDate에_해당하는_amount_캐시를_amount만큼_증가시킨다() throws Exception {
            // given
            hashCacheService.putDataInHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate(),
                    1000L);

            // when
            targetService.updateStatisticsCacheFromNewData(new ArticleCreatedEvent(article, user));

            // then
            Object sum = hashCacheService.getDataFromHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate());
            assertThat(sum).isEqualTo(1000L + article.getAmount());
        }

        @Test
        void User의_gender에_해당하는_satisfaction_캐시를_satisfaction만큼_증가시킨다() throws Exception {
            // given
            hashCacheService.putDataInHash(
                    GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                    user.getGender().name(),
                    5.0);
            hashCacheService.putDataInHash(
                    GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                    user.getGender().name(),
                    1L);

            // when
            targetService.updateStatisticsCacheFromNewData(new ArticleCreatedEvent(article, user));

            // then
            Object sum = hashCacheService.getDataFromHash(
                    GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                    user.getGender().name());
            Object count = hashCacheService.getDataFromHash(
                    GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                    user.getGender().name());
            assertThat(sum).isEqualTo(5.0 + article.getSatisfaction());
            assertThat(count).isEqualTo(2L);
        }
    }

    @Nested
    class updateStatisticsCacheFromModifiedData{

    }

    @Nested
    class updateStatisticsCacheFromRemovedData{
        @Test
        void User의_gender과_Article의_emotion에_해당하는_amount_캐시를_amount만큼_감소시킨다() throws Exception {
            // given
            hashCacheService.putDataInHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1000L);
            hashCacheService.putDataInHash(
                    GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1L);

            // when
            targetService.updateStatisticsCacheFromRemovedData(new ArticleDeletedEvent(article, user));

            // then
            Object sum = hashCacheService.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            Object count = hashCacheService.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            assertThat(sum).isEqualTo(1000L - article.getAmount());
            assertThat(count).isEqualTo(0L);
        }

        @Test
        void User의_gender와_Article의_spendDate에_해당하는_amount_캐시를_amount만큼_감소시킨다() throws Exception {
            // given
            hashCacheService.putDataInHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate(),
                    1000L);

            // when
            targetService.updateStatisticsCacheFromRemovedData(new ArticleDeletedEvent(article, user));

            // then
            Object sum = hashCacheService.getDataFromHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate());
            assertThat(sum).isEqualTo(1000L - article.getAmount());
        }

        @Test
        void User의_gender에_해당하는_satisfaction_캐시를_satisfaction만큼_감소시킨다() throws Exception {
            // given
            hashCacheService.putDataInHash(
                    GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                    user.getGender().name(),
                    5.0);
            hashCacheService.putDataInHash(
                    GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                    user.getGender().name(),
                    1L);

            // when
            targetService.updateStatisticsCacheFromRemovedData(new ArticleDeletedEvent(article, user));

            // then
            Object sum = hashCacheService.getDataFromHash(
                    GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                    user.getGender().name());
            Object count = hashCacheService.getDataFromHash(
                    GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                    user.getGender().name());
            assertThat(sum).isEqualTo(5.0 - article.getSatisfaction());
            assertThat(count).isEqualTo(0L);
        }
    }
}