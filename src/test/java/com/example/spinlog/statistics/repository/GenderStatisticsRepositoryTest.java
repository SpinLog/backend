package com.example.spinlog.statistics.repository;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.statistics.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.statistics.dto.MemoDto;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import com.example.spinlog.util.ArticleFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsRepositoryTest {
    @Autowired
    GenderStatisticsRepository genderStatisticsRepository;

    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em; // flush 하기 위해 필요 (JPA 쓰기 지연 방지)

    /**
     * DB가 데이터를 필터링 해서 반환하는데, 필터링 됐음을 유저의 Gender로 구분한다.
     * MALE -> 필터링 되지 않고 그대로 받환되는 데이터의 Gender
     * FEMALE -> 필터링 되어 받을 수 없는 데이터의 Gender
     * */
    User survivedUser;
    Gender survivedGender = Gender.MALE;
    User filteredUser;
    Gender filteredGender = Gender.FEMALE;

    LocalDate startDate, endDate;

    /**
     * 필터링 결과를 구분하기 위해, 필터링 대상 MBTI를 가지고 있는 유저 객체 2개를 생성한다.
     * */
    @BeforeEach
    public void createTwoUserToDivideFilteringResultAndLocalDateRange() {
        survivedUser = User.builder()
                .email("survived@email")
                .authenticationName("survivedUser")
                .mbti(Mbti.ISTJ)
                .gender(survivedGender)
                .build();
        filteredUser = User.builder()
                .email("filtered@email")
                .authenticationName("filteredUser")
                .mbti(Mbti.ENFP)
                .gender(filteredGender) // 정반대의 Gender 입력 -> 이 gender는 필터링 되는 gender
                .build();
        userRepository.save(survivedUser);
        userRepository.save(filteredUser);

        startDate = LocalDate.now().minusDays(2);
        endDate = LocalDate.now().minusDays(1);
    }

    @Nested
    class 성별_감정별_금액_평균을_반환하는_메서드 {
        @Test
        void 입력받은_startDate와_endDate를_기준으로_데이터를_필터링해서_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            Emotion emotion = Emotion.PROUD;
            List<Article> removedArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(startDate.atStartOfDay().minusSeconds(1L))
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            List<Article> survivedArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(startDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay().minusSeconds(1L))
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<GenderEmotionAmountAverageDto> dtos = genderStatisticsRepository.getAmountAveragesEachGenderAndEmotionBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos.stream()
                    .map(GenderEmotionAmountAverageDto::getGender)
                    .distinct()
                    .toList())
                    .containsOnly(survivedGender)
                    .doesNotContain(filteredGender);
        }

        @Test
        void 입력받은_registerType을_기준으로_데이터를_필터링해서_반환한다() throws Exception {
            // given
            RegisterType survivedRegisterType = RegisterType.SPEND;
            RegisterType filteredRegisterType = RegisterType.SAVE;
            Emotion emotion = Emotion.PROUD;
            List<Article> removedArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(filteredRegisterType)
                                    .emotion(emotion)
                                    .spendDate(startDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(filteredRegisterType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            List<Article> survivedArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(survivedRegisterType)
                                    .emotion(emotion)
                                    .spendDate(startDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(survivedRegisterType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<GenderEmotionAmountAverageDto> dtos = genderStatisticsRepository.getAmountAveragesEachGenderAndEmotionBetweenStartDateAndEndDate(
                    survivedRegisterType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos.stream()
                    .map(GenderEmotionAmountAverageDto::getGender)
                    .distinct()
                    .toList())
                    .containsOnly(survivedGender)
                    .doesNotContain(filteredGender);
        }

        @Test
        @Order(1)
        void 성별_감정별_금액의_평균을_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            Emotion emotion = Emotion.PROUD;
            List<Article> articles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(startDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay().minusSeconds(1L))
                                    .amount(2000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay().minusSeconds(1L))
                                    .amount(3000)
                                    .build()
                                    .createArticle()));
            Long amountAverage = (long) articles.stream()
                    .map(Article::getAmount)
                    .reduce(Integer::sum)
                    .get() / articles.size();

            em.flush();

            // when
            List<GenderEmotionAmountAverageDto> dtos = genderStatisticsRepository.getAmountAveragesEachGenderAndEmotionBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos)
                    .extracting(GenderEmotionAmountAverageDto::getEmotion)
                    .containsOnly(emotion);
            assertThat(dtos)
                    .extracting(GenderEmotionAmountAverageDto::getAmountAverage)
                    .containsOnly(amountAverage);
        }
    }

    @Nested
    class 성별_일별_금액_합을_반환하는_메서드 {
        @Test
        void 입력받은_startDate와_endDate를_기준으로_데이터를_필터링해서_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            List<Article> filteredArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(registerType)
                                    .spendDate(startDate.atStartOfDay().minusSeconds(1L))
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            List<Article> survivedArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(startDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay().minusSeconds(1L))
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<GenderDailyAmountSumDto> dtos = genderStatisticsRepository.getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos.stream()
                    .map(GenderDailyAmountSumDto::getGender)
                    .distinct()
                    .toList())
                    .containsOnly(survivedGender)
                    .doesNotContain(filteredGender);
        }

        @Test
        void 입력받은_registerType을_기준으로_데이터를_필터링해서_반환한다() throws Exception {
            // given
            RegisterType filteredRegisterType = RegisterType.SPEND;
            RegisterType survivedRegisterType = RegisterType.SAVE;
            List<Article> filteredArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(filteredRegisterType)
                                    .spendDate(startDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(filteredRegisterType)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            List<Article> survivedArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(survivedRegisterType)
                                    .spendDate(startDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(survivedRegisterType)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<GenderDailyAmountSumDto> dtos = genderStatisticsRepository.getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
                    survivedRegisterType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos.stream()
                    .map(GenderDailyAmountSumDto::getGender)
                    .distinct()
                    .toList())
                    .containsOnly(survivedGender)
                    .doesNotContain(filteredGender);
        }

        @Test
        void 성별_일별_금액의_합을_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            Emotion emotion = Emotion.PROUD;
            List<Article> articles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay().minusSeconds(1L))
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay().minusSeconds(1L))
                                    .amount(2000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay().minusSeconds(1L))
                                    .amount(3000)
                                    .build()
                                    .createArticle()));
            long sum = articles.stream()
                    .mapToLong(Article::getAmount)
                    .reduce(Long::sum).orElseGet(() -> -1L);
            em.flush();

            // when
            List<GenderDailyAmountSumDto> dtos = genderStatisticsRepository.getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos)
                    .extracting(GenderDailyAmountSumDto::getAmountSum)
                    .containsOnly(sum);
        }
    }
    @Nested
    class 메모를_반환하는_메서드 {
        @Test
        void 입력받은_registerType을_기준으로_데이터를_필터링해서_반환한다() throws Exception {
            // given
            RegisterType filteredRegisterType = RegisterType.SAVE;
            RegisterType survivedRegisterType = RegisterType.SPEND;
            String survivedContent = "survivedContent";
            String filteredContent = "filteredContent";
            List<Article> filteredArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(filteredRegisterType)
                                    .spendDate(startDate.atStartOfDay())
                                    .content(filteredContent)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(filteredRegisterType)
                                    .spendDate(endDate.atStartOfDay())
                                    .content(filteredContent)
                                    .build()
                                    .createArticle()));
            List<Article> survivedArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(survivedRegisterType)
                                    .spendDate(startDate.atStartOfDay())
                                    .content(survivedContent)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(survivedRegisterType)
                                    .spendDate(endDate.atStartOfDay())
                                    .content(survivedContent)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<MemoDto> dtos = genderStatisticsRepository.getAllMemosByGenderBetweenStartDateAndEndDate(
                    survivedRegisterType,
                    Gender.MALE,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos)
                    .extracting(MemoDto::getContent)
                    .containsOnly(survivedContent)
                    .doesNotContain(filteredContent);
        }

        @Test
        void 입력받은_startDate와_endDate를_기준으로_데이터를_필터링해서_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            String survivedContent = "survivedContent";
            String filteredContent = "filteredContent";
            List<Article> filteredArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(registerType)
                                    .spendDate(startDate.atStartOfDay().minusSeconds(1L))
                                    .content(filteredContent)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay().plusSeconds(1L))
                                    .content(filteredContent)
                                    .build()
                                    .createArticle()));
            List<Article> survivedArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(startDate.atStartOfDay())
                                    .content(survivedContent)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay())
                                    .content(survivedContent)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<MemoDto> dtos = genderStatisticsRepository.getAllMemosByGenderBetweenStartDateAndEndDate(
                    registerType,
                    Gender.MALE,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos)
                    .extracting(MemoDto::getContent)
                    .containsOnly(survivedContent)
                    .doesNotContain(filteredContent);
        }

        @Test
        void 입력받은_성별에_해당하는_유저들의_메모만_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            String survivedContent = "survivedContent";
            String filteredContent = "filteredContent";
            List<Article> filteredArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(registerType)
                                    .spendDate(startDate.atStartOfDay())
                                    .content(filteredContent)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay())
                                    .content(filteredContent)
                                    .build()
                                    .createArticle()));
            List<Article> survivedArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(startDate.atStartOfDay())
                                    .content(survivedContent)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay())
                                    .content(survivedContent)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<MemoDto> dtos = genderStatisticsRepository.getAllMemosByGenderBetweenStartDateAndEndDate(
                    registerType,
                    Gender.MALE,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos)
                    .extracting(MemoDto::getContent)
                    .containsOnly(survivedContent)
                    .doesNotContain(filteredContent);
        }
    }
    @Nested
    class 성별_만족도_평균을_반환하는_메서드 {
        @Test
        void 입력받은_startDate와_endDate를_기준으로_데이터를_필터링해서_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            List<Article> filteredArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(registerType)
                                    .spendDate(startDate.atStartOfDay().minusSeconds(1L))
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            List<Article> survivedArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(startDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay().minusSeconds(1L))
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<GenderSatisfactionAverageDto> dtos = genderStatisticsRepository.getSatisfactionAveragesEachGenderBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos)
                    .extracting(GenderSatisfactionAverageDto::getGender)
                    .contains(survivedGender)
                    .doesNotContain(filteredGender);
        }

        @Test
        void 입력받은_registerType을_기준으로_데이터를_필터링해서_반환한다() throws Exception {
            // given
            RegisterType filteredRegisterType = RegisterType.SAVE;
            RegisterType survivedRegisterType = RegisterType.SPEND;
            List<Article> filteredArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(filteredRegisterType)
                                    .spendDate(startDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(filteredUser)
                                    .registerType(filteredRegisterType)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            List<Article> survivedArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(survivedRegisterType)
                                    .spendDate(startDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(survivedRegisterType)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<GenderSatisfactionAverageDto> dtos = genderStatisticsRepository.getSatisfactionAveragesEachGenderBetweenStartDateAndEndDate(
                    survivedRegisterType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos)
                    .extracting(GenderSatisfactionAverageDto::getGender)
                    .contains(survivedGender)
                    .doesNotContain(filteredGender);
        }

        @Test
        @Order(1)
        void 성별_만족도의_평균을_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            List<Article> articles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay().minusSeconds(1L))
                                    .satisfaction(3.0f)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay().minusSeconds(1L))
                                    .satisfaction(4.0f)
                                    .build()
                                    .createArticle()));
            Float satisfactionAverage = articles.stream()
                    .map(Article::getSatisfaction)
                    .reduce(Float::sum)
                    .orElseGet(() -> 0f)
                    / articles.size();
            em.flush();

            // when
            List<GenderSatisfactionAverageDto> dtos = genderStatisticsRepository.getSatisfactionAveragesEachGenderBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(
                    dtos.stream()
                            .map(GenderSatisfactionAverageDto::getSatisfactionAverage)
                            .toList()
            ).containsOnly(satisfactionAverage);
        }
    }
}