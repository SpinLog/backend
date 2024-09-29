package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.event.ArticleCreatedEvent;
import com.example.spinlog.article.event.ArticleDeletedEvent;
import com.example.spinlog.global.cache.CacheService;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.util.ArticleFactory;
import com.example.spinlog.util.MockCacheService;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static com.example.spinlog.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.utils.CacheKeyNameUtils.getGenderStatisticsSatisfactionCountKeyName;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsCacheSynchronizerTest {
    CacheService cacheService = spy(MockCacheService.class);
    GenderStatisticsCacheSynchronizer targetService =
            new GenderStatisticsCacheSynchronizer(cacheService);

    RegisterType registerType = RegisterType.SPEND;
    User user = User.builder()
            .gender(Gender.MALE)
            .authenticationName("test")
            .email("email@email")
            .build();
    Article article = ArticleFactory.builder()
            .spendDate(LocalDateTime.now())
            .emotion(Emotion.PROUD)
            .amount(5)
            .satisfaction(5.0f)
            .build()
            .createArticle();

    @BeforeEach
    void setUp() {
        cacheService.putDataInHash(
                getGenderEmotionStatisticsAmountSumKeyName(registerType),
                user.getGender() + "::" + article.getEmotion(), 0L);
        cacheService.putDataInHash(
                getGenderEmotionStatisticsAmountCountKeyName(registerType),
                user.getGender() + "::" + article.getEmotion(), 0L);

        cacheService.putDataInHash(
                getGenderDailyStatisticsAmountSumKeyName(registerType),
                user.getGender() + "::" + article.getSpendDate().toLocalDate(), 0L);

        cacheService.putDataInHash(
                getGenderStatisticsSatisfactionSumKeyName(registerType),
                user.getGender().name(), 0.0);
        cacheService.putDataInHash(
                getGenderStatisticsSatisfactionCountKeyName(registerType),
                user.getGender().name(), 0L);
    }

    @Nested
    class updateStatisticsCacheFromNewData{
        @Test
        void User의_gender과_Article의_emotion에_해당하는_amount_캐시를_amount만큼_증가시킨다() throws Exception {
            // given
            cacheService.putDataInHash(
                    getGenderEmotionStatisticsAmountSumKeyName(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1000L);
            cacheService.putDataInHash(
                    getGenderEmotionStatisticsAmountCountKeyName(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1L);

            // when
            targetService.updateStatisticsCacheFromNewData(new ArticleCreatedEvent(article, user));

            // then
            Object sum = cacheService.getDataFromHash(
                    getGenderEmotionStatisticsAmountSumKeyName(registerType),
                    user.getGender() + "::" + article.getEmotion());
            Object count = cacheService.getDataFromHash(
                    getGenderEmotionStatisticsAmountCountKeyName(registerType),
                    user.getGender() + "::" + article.getEmotion());
            assertThat(sum).isEqualTo(1000L + article.getAmount());
            assertThat(count).isEqualTo(2L);
        }

        @Test
        void User의_gender와_Article의_spendDate에_해당하는_amount_캐시를_amount만큼_증가시킨다() throws Exception {
            // given
            cacheService.putDataInHash(
                    getGenderDailyStatisticsAmountSumKeyName(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate(),
                    1000L);

            // when
            targetService.updateStatisticsCacheFromNewData(new ArticleCreatedEvent(article, user));

            // then
            Object sum = cacheService.getDataFromHash(
                    getGenderDailyStatisticsAmountSumKeyName(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate());
            assertThat(sum).isEqualTo(1000L + article.getAmount());
        }

        @Test
        void User의_gender에_해당하는_satisfaction_캐시를_satisfaction만큼_증가시킨다() throws Exception {
            // given
            cacheService.putDataInHash(
                    getGenderStatisticsSatisfactionSumKeyName(registerType),
                    user.getGender().name(),
                    5.0);
            cacheService.putDataInHash(
                    getGenderStatisticsSatisfactionCountKeyName(registerType),
                    user.getGender().name(),
                    1L);

            // when
            targetService.updateStatisticsCacheFromNewData(new ArticleCreatedEvent(article, user));

            // then
            Object sum = cacheService.getDataFromHash(
                    getGenderStatisticsSatisfactionSumKeyName(registerType),
                    user.getGender().name());
            Object count = cacheService.getDataFromHash(
                    getGenderStatisticsSatisfactionCountKeyName(registerType),
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
            cacheService.putDataInHash(
                    getGenderEmotionStatisticsAmountSumKeyName(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1000L);
            cacheService.putDataInHash(
                    getGenderEmotionStatisticsAmountCountKeyName(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1L);

            // when
            targetService.updateStatisticsCacheFromRemovedData(new ArticleDeletedEvent(article, user));

            // then
            Object sum = cacheService.getDataFromHash(
                    getGenderEmotionStatisticsAmountSumKeyName(registerType),
                    user.getGender() + "::" + article.getEmotion());
            Object count = cacheService.getDataFromHash(
                    getGenderEmotionStatisticsAmountCountKeyName(registerType),
                    user.getGender() + "::" + article.getEmotion());
            assertThat(sum).isEqualTo(1000L - article.getAmount());
            assertThat(count).isEqualTo(0L);
        }

        @Test
        void User의_gender와_Article의_spendDate에_해당하는_amount_캐시를_amount만큼_감소시킨다() throws Exception {
            // given
            cacheService.putDataInHash(
                    getGenderDailyStatisticsAmountSumKeyName(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate(),
                    1000L);

            // when
            targetService.updateStatisticsCacheFromRemovedData(new ArticleDeletedEvent(article, user));

            // then
            Object sum = cacheService.getDataFromHash(
                    getGenderDailyStatisticsAmountSumKeyName(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate());
            assertThat(sum).isEqualTo(1000L - article.getAmount());
        }

        @Test
        void User의_gender에_해당하는_satisfaction_캐시를_satisfaction만큼_감소시킨다() throws Exception {
            // given
            cacheService.putDataInHash(
                    getGenderStatisticsSatisfactionSumKeyName(registerType),
                    user.getGender().name(),
                    5.0);
            cacheService.putDataInHash(
                    getGenderStatisticsSatisfactionCountKeyName(registerType),
                    user.getGender().name(),
                    1L);

            // when
            targetService.updateStatisticsCacheFromRemovedData(new ArticleDeletedEvent(article, user));

            // then
            Object sum = cacheService.getDataFromHash(
                    getGenderStatisticsSatisfactionSumKeyName(registerType),
                    user.getGender().name());
            Object count = cacheService.getDataFromHash(
                    getGenderStatisticsSatisfactionCountKeyName(registerType),
                    user.getGender().name());
            assertThat(sum).isEqualTo(5.0 - article.getSatisfaction());
            assertThat(count).isEqualTo(0L);
        }
    }
}