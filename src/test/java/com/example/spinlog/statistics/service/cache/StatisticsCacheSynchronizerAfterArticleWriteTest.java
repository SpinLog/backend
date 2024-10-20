package com.example.spinlog.statistics.service.cache;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.event.ArticleCreatedEvent;
import com.example.spinlog.article.event.ArticleDeletedEvent;
import com.example.spinlog.article.event.ArticleUpdatedEvent;
import com.example.spinlog.global.cache.CacheHashRepository;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.service.StatisticsPeriodManager;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.util.ArticleFactory;
import com.example.spinlog.util.MockCacheHashRepository;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.LocalDateTime;

import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.*;
import static com.example.spinlog.statistics.utils.CacheKeyNameUtils.GENDER_SATISFACTION_COUNT_KEY_NAME;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StatisticsCacheSynchronizerAfterArticleWriteTest {
    CacheHashRepository cacheHashRepository = spy(MockCacheHashRepository.class);
    StatisticsPeriodManager statisticsPeriodManager = new StatisticsPeriodManager(Clock.systemDefaultZone());
    StatisticsCacheSynchronizerAfterArticleWrite targetService =
            new StatisticsCacheSynchronizerAfterArticleWrite(
                    cacheHashRepository,
                    statisticsPeriodManager);

    RegisterType registerType = RegisterType.SPEND;
    Mbti mbti = Mbti.ISTJ;
    User user = User.builder()
                .gender(Gender.MALE)
                .mbti(mbti)
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
        cacheHashRepository.putDataInHash(
                GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                user.getGender() + "::" + article.getEmotion(), 0L);
        cacheHashRepository.putDataInHash(
                GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                user.getGender() + "::" + article.getEmotion(), 0L);

        cacheHashRepository.putDataInHash(
                GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                user.getGender() + "::" + article.getSpendDate().toLocalDate(), 0L);

        cacheHashRepository.putDataInHash(
                GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                user.getGender().name(), 0.0);
        cacheHashRepository.putDataInHash(
                GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                user.getGender().name(), 0L);

        for(char mbtiFactor: mbti.toString().toCharArray()){
            cacheHashRepository.putDataInHash(
                    MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    mbtiFactor + "::" + article.getEmotion(), 0L);
            cacheHashRepository.putDataInHash(
                    MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    mbtiFactor + "::" + article.getEmotion(), 0L);

            cacheHashRepository.putDataInHash(
                    MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    mbtiFactor + "::" + article.getSpendDate().toLocalDate(), 0L);

            cacheHashRepository.putDataInHash(
                    MBTI_SATISFACTION_SUM_KEY_NAME(registerType),
                    String.valueOf(mbtiFactor), 0.0);
            cacheHashRepository.putDataInHash(
                    MBTI_SATISFACTION_COUNT_KEY_NAME(registerType),
                    String.valueOf(mbtiFactor), 0L);

        }
    }

    @Nested
    class updateStatisticsCacheFromNewData{
        @Test
        void User의_gender과_Article의_emotion에_해당하는_amount_캐시를_amount만큼_증가시킨다() throws Exception {
            // given
            cacheHashRepository.putDataInHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1000L);
            cacheHashRepository.putDataInHash(
                    GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1L);

            // when
            targetService.updateStatisticsCacheFromCreatedData(new ArticleCreatedEvent(article, user));

            // then
            Object sum = cacheHashRepository.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            Object count = cacheHashRepository.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            assertThat(sum).isEqualTo(1000L + article.getAmount());
            assertThat(count).isEqualTo(2L);
        }

        @Test
        void User의_gender와_Article의_spendDate에_해당하는_amount_캐시를_amount만큼_증가시킨다() throws Exception {
            // given
            cacheHashRepository.putDataInHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate(),
                    1000L);

            // when
            targetService.updateStatisticsCacheFromCreatedData(new ArticleCreatedEvent(article, user));

            // then
            Object sum = cacheHashRepository.getDataFromHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate());
            assertThat(sum).isEqualTo(1000L + article.getAmount());
        }

        @Test
        void User의_gender에_해당하는_satisfaction_캐시를_satisfaction만큼_증가시킨다() throws Exception {
            // given
            cacheHashRepository.putDataInHash(
                    GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                    user.getGender().name(),
                    5.0);
            cacheHashRepository.putDataInHash(
                    GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                    user.getGender().name(),
                    1L);

            // when
            targetService.updateStatisticsCacheFromCreatedData(new ArticleCreatedEvent(article, user));

            // then
            Object sum = cacheHashRepository.getDataFromHash(
                    GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                    user.getGender().name());
            Object count = cacheHashRepository.getDataFromHash(
                    GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                    user.getGender().name());
            assertThat(sum).isEqualTo(5.0 + article.getSatisfaction());
            assertThat(count).isEqualTo(2L);
        }

        @Test
        void User의_mbti에_해당하는_캐시를_업데이트한다() throws Exception {
            // when
            targetService.updateStatisticsCacheFromCreatedData(new ArticleCreatedEvent(article, user));

            // then
            for(char mbtiFactor: user.getMbti().toString().toCharArray()){
                Object mbtiSum = cacheHashRepository.getDataFromHash(
                        MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion());
                Object mbtiCount = cacheHashRepository.getDataFromHash(
                        MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion());
                assertThat(mbtiSum).isEqualTo(article.getAmount().longValue());
                assertThat(mbtiCount).isEqualTo(1L);

                Object mbtiDailySum = cacheHashRepository.getDataFromHash(
                        MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getSpendDate().toLocalDate());
                assertThat(mbtiDailySum).isEqualTo(article.getAmount().longValue());

                Object mbtiSatisfactionSum = cacheHashRepository.getDataFromHash(
                        MBTI_SATISFACTION_SUM_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor));
                Object mbtiSatisfactionCount = cacheHashRepository.getDataFromHash(
                        MBTI_SATISFACTION_COUNT_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor));
                assertThat(mbtiSatisfactionSum).isEqualTo(article.getSatisfaction().doubleValue());
                assertThat(mbtiSatisfactionCount).isEqualTo(1L);
            }
        }

        @Test
        void Article의_spendDate가_PERIOD_CRITERIA의_범위가_아니라면_캐시가_업데이트_되지_않는다() throws Exception {
            // given
            Article article = ArticleFactory.builder()
                    .spendDate(LocalDateTime.now().minusDays(30 + 1))
                    .emotion(Emotion.PROUD)
                    .amount(5)
                    .satisfaction(5.0f)
                    .build()
                    .createArticle();

            // when
            targetService.updateStatisticsCacheFromCreatedData(new ArticleCreatedEvent(article, user));

            // then
            Object sum = cacheHashRepository.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            Object count = cacheHashRepository.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            assertThat(sum).isEqualTo(0L);
            assertThat(count).isEqualTo(0L);

            sum = cacheHashRepository.getDataFromHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate());
            assertThat(sum).isNull();

            sum = cacheHashRepository.getDataFromHash(
                    GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                    user.getGender().name());
            count = cacheHashRepository.getDataFromHash(
                    GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                    user.getGender().name());
            assertThat(sum).isEqualTo(0.0);
            assertThat(count).isEqualTo(0L);

            for(char mbtiFactor: user.getMbti().toString().toCharArray()){
                Object mbtiSum = cacheHashRepository.getDataFromHash(
                        MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion());
                Object mbtiCount = cacheHashRepository.getDataFromHash(
                        MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion());
                assertThat(mbtiSum).isEqualTo(0L);
                assertThat(mbtiCount).isEqualTo(0L);

                Object mbtiDailySum = cacheHashRepository.getDataFromHash(
                        MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getSpendDate().toLocalDate());
                assertThat(mbtiDailySum).isNull();

                Object mbtiSatisfactionSum = cacheHashRepository.getDataFromHash(
                        MBTI_SATISFACTION_SUM_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor));
                Object mbtiSatisfactionCount = cacheHashRepository.getDataFromHash(
                        MBTI_SATISFACTION_COUNT_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor));
                assertThat(mbtiSatisfactionSum).isEqualTo(0.0);
                assertThat(mbtiSatisfactionCount).isEqualTo(0L);
            }
        }
    }

    @Nested
    class updateStatisticsCacheFromModifiedData{
        @Test
        void User의_gender에_해당하는_캐시가_업데이트된다() throws Exception {
            // given
            cacheHashRepository.putDataInHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1500L);
            cacheHashRepository.putDataInHash(
                    GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    2L);
            cacheHashRepository.putDataInHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate(),
                    1000L);
            cacheHashRepository.putDataInHash(
                    GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                    user.getGender().name(),
                    10.0);
            cacheHashRepository.putDataInHash(
                    GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                    user.getGender().name(),
                    2L);

            Article updatedArticle = ArticleFactory.builder()
                    .amount(100)
                    .satisfaction(2.0f)
                    .registerType(article.getRegisterType())
                    .spendDate(article.getSpendDate())
                    .emotion(article.getEmotion())
                    .build()
                    .createArticle();

            // when
            targetService.updateStatisticsCacheFromModifiedData(new ArticleUpdatedEvent(article, updatedArticle, user));

            // then
            Object sum = cacheHashRepository.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            Object count = cacheHashRepository.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            assertThat(sum).isEqualTo(1500L - article.getAmount() + updatedArticle.getAmount());
            assertThat(count).isEqualTo(2L);

            sum = cacheHashRepository.getDataFromHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate());
            assertThat(sum).isEqualTo(1000L - article.getAmount() + updatedArticle.getAmount());

            sum = cacheHashRepository.getDataFromHash(
                    GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                    user.getGender().name());
            count = cacheHashRepository.getDataFromHash(
                    GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                    user.getGender().name());
            assertThat(sum).isEqualTo(10.0 - article.getSatisfaction() + updatedArticle.getSatisfaction());
            assertThat(count).isEqualTo(2L);
        }

        @Test
        void User의_mbti에_해당하는_캐시가_업데이트된다() throws Exception {
            // given
            for(char mbtiFactor: user.getMbti().toString().toCharArray()){
                cacheHashRepository.putDataInHash(
                        MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion(),
                        1500L);
                cacheHashRepository.putDataInHash(
                        MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion(),
                        2L);
                cacheHashRepository.putDataInHash(
                        MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getSpendDate().toLocalDate(),
                        1000L);
                cacheHashRepository.putDataInHash(
                        MBTI_SATISFACTION_SUM_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor),
                        10.0);
                cacheHashRepository.putDataInHash(
                        MBTI_SATISFACTION_COUNT_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor),
                        2L);
            }
            Article updatedArticle = ArticleFactory.builder()
                    .amount(100)
                    .satisfaction(2.0f)
                    .registerType(article.getRegisterType())
                    .spendDate(article.getSpendDate())
                    .emotion(article.getEmotion())
                    .build()
                    .createArticle();

            // when
            targetService.updateStatisticsCacheFromModifiedData(new ArticleUpdatedEvent(article, updatedArticle, user));

            // then
            for(char mbtiFactor: user.getMbti().toString().toCharArray()){
                Object mbtiSum = cacheHashRepository.getDataFromHash(
                        MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion());
                Object mbtiCount = cacheHashRepository.getDataFromHash(
                        MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion());
                assertThat(mbtiSum).isEqualTo(1500L - article.getAmount() + updatedArticle.getAmount());
                assertThat(mbtiCount).isEqualTo(2L);

                Object mbtiDailySum = cacheHashRepository.getDataFromHash(
                        MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getSpendDate().toLocalDate());
                assertThat(mbtiDailySum).isEqualTo(1000L - article.getAmount() + updatedArticle.getAmount());

                Object mbtiSatisfactionSum = cacheHashRepository.getDataFromHash(
                        MBTI_SATISFACTION_SUM_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor));
                Object mbtiSatisfactionCount = cacheHashRepository.getDataFromHash(
                        MBTI_SATISFACTION_COUNT_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor));
                assertThat(mbtiSatisfactionSum).isEqualTo(10.0 - article.getSatisfaction() + updatedArticle.getSatisfaction());
                assertThat(mbtiSatisfactionCount).isEqualTo(2L);
            }
        }
    }

    @Nested
    class updateStatisticsCacheFromRemovedData{
        @Test
        void User의_gender과_Article의_emotion에_해당하는_amount_캐시를_amount만큼_감소시킨다() throws Exception {
            // given
            cacheHashRepository.putDataInHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1000L);
            cacheHashRepository.putDataInHash(
                    GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion(),
                    1L);

            // when
            targetService.updateStatisticsCacheFromRemovedData(new ArticleDeletedEvent(article, user));

            // then
            Object sum = cacheHashRepository.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            Object count = cacheHashRepository.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            assertThat(sum).isEqualTo(1000L - article.getAmount());
            assertThat(count).isEqualTo(0L);
        }

        @Test
        void User의_gender와_Article의_spendDate에_해당하는_amount_캐시를_amount만큼_감소시킨다() throws Exception {
            // given
            cacheHashRepository.putDataInHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate(),
                    1000L);

            // when
            targetService.updateStatisticsCacheFromRemovedData(new ArticleDeletedEvent(article, user));

            // then
            Object sum = cacheHashRepository.getDataFromHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate());
            assertThat(sum).isEqualTo(1000L - article.getAmount());
        }

        @Test
        void User의_gender에_해당하는_satisfaction_캐시를_satisfaction만큼_감소시킨다() throws Exception {
            // given
            cacheHashRepository.putDataInHash(
                    GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                    user.getGender().name(),
                    5.0);
            cacheHashRepository.putDataInHash(
                    GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                    user.getGender().name(),
                    1L);

            // when
            targetService.updateStatisticsCacheFromRemovedData(new ArticleDeletedEvent(article, user));

            // then
            Object sum = cacheHashRepository.getDataFromHash(
                    GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                    user.getGender().name());
            Object count = cacheHashRepository.getDataFromHash(
                    GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                    user.getGender().name());
            assertThat(sum).isEqualTo(5.0 - article.getSatisfaction());
            assertThat(count).isEqualTo(0L);
        }

        @Test
        void User의_mbti에_해당하는_캐시를_업데이트한다() throws Exception {
            // given
            for(char mbtiFactor: user.getMbti().toString().toCharArray()){
                cacheHashRepository.putDataInHash(
                        MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion(),
                        1000L + article.getAmount().longValue());
                cacheHashRepository.putDataInHash(
                        MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion(),
                        2L);

                cacheHashRepository.putDataInHash(
                        MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getSpendDate().toLocalDate(),
                        1000L + article.getAmount().longValue());

                cacheHashRepository.putDataInHash(
                        MBTI_SATISFACTION_SUM_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor),
                        10.0 + article.getSatisfaction().doubleValue());
                cacheHashRepository.putDataInHash(
                        MBTI_SATISFACTION_COUNT_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor),
                        3L);
            }

            // when
            targetService.updateStatisticsCacheFromRemovedData(new ArticleDeletedEvent(article, user));

            // then
            for(char mbtiFactor: user.getMbti().toString().toCharArray()){
                Object mbtiSum = cacheHashRepository.getDataFromHash(
                        MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion());
                Object mbtiCount = cacheHashRepository.getDataFromHash(
                        MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion());
                assertThat(mbtiSum).isEqualTo(1000L);
                assertThat(mbtiCount).isEqualTo(1L);

                Object mbtiDailySum = cacheHashRepository.getDataFromHash(
                        MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getSpendDate().toLocalDate());
                assertThat(mbtiDailySum).isEqualTo(1000L);

                Object mbtiSatisfactionSum = cacheHashRepository.getDataFromHash(
                        MBTI_SATISFACTION_SUM_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor));
                Object mbtiSatisfactionCount = cacheHashRepository.getDataFromHash(
                        MBTI_SATISFACTION_COUNT_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor));
                assertThat(mbtiSatisfactionSum).isEqualTo(10.0);
                assertThat(mbtiSatisfactionCount).isEqualTo(2L);
            }
        }

        @Test
        void Article의_spendDate가_PERIOD_CRITERIA의_범위가_아니라면_캐시가_업데이트_되지_않는다() throws Exception {
            // given
            Article article = ArticleFactory.builder()
                    .spendDate(LocalDateTime.now().minusDays(30 + 1))
                    .emotion(Emotion.PROUD)
                    .amount(5)
                    .satisfaction(5.0f)
                    .build()
                    .createArticle();

            // when
            targetService.updateStatisticsCacheFromRemovedData(new ArticleDeletedEvent(article, user));

            // then
            Object sum = cacheHashRepository.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            Object count = cacheHashRepository.getDataFromHash(
                    GENDER_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getEmotion());
            assertThat(sum).isEqualTo(0L);
            assertThat(count).isEqualTo(0L);

            sum = cacheHashRepository.getDataFromHash(
                    GENDER_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                    user.getGender() + "::" + article.getSpendDate().toLocalDate());
            assertThat(sum).isNull();

            sum = cacheHashRepository.getDataFromHash(
                    GENDER_SATISFACTION_SUM_KEY_NAME(registerType),
                    user.getGender().name());
            count = cacheHashRepository.getDataFromHash(
                    GENDER_SATISFACTION_COUNT_KEY_NAME(registerType),
                    user.getGender().name());
            assertThat(sum).isEqualTo(0.0);
            assertThat(count).isEqualTo(0L);

            for(char mbtiFactor: user.getMbti().toString().toCharArray()){
                Object mbtiSum = cacheHashRepository.getDataFromHash(
                        MBTI_EMOTION_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion());
                Object mbtiCount = cacheHashRepository.getDataFromHash(
                        MBTI_EMOTION_AMOUNT_COUNT_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getEmotion());
                assertThat(mbtiSum).isEqualTo(0L);
                assertThat(mbtiCount).isEqualTo(0L);

                Object mbtiDailySum = cacheHashRepository.getDataFromHash(
                        MBTI_DAILY_AMOUNT_SUM_KEY_NAME(registerType),
                        mbtiFactor + "::" + article.getSpendDate().toLocalDate());
                assertThat(mbtiDailySum).isNull();

                Object mbtiSatisfactionSum = cacheHashRepository.getDataFromHash(
                        MBTI_SATISFACTION_SUM_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor));
                Object mbtiSatisfactionCount = cacheHashRepository.getDataFromHash(
                        MBTI_SATISFACTION_COUNT_KEY_NAME(registerType),
                        String.valueOf(mbtiFactor));
                assertThat(mbtiSatisfactionSum).isEqualTo(0.0);
                assertThat(mbtiSatisfactionCount).isEqualTo(0L);
            }
        }
    }
}