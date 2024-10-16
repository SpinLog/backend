package com.example.spinlog.statistics.repository;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.dto.MBTIDailyAmountSumDto;
import com.example.spinlog.statistics.dto.MBTIEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.MBTISatisfactionAverageDto;
import com.example.spinlog.statistics.dto.MemoDto;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import com.example.spinlog.util.ArticleFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@Transactional
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MBTIStatisticsRepositoryTest {
    @Autowired
    MBTIStatisticsRepository mbtiStatisticsRepository;

    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em; // flush 하기 위해 필요 (JPA 쓰기 지연 방지)

    /**
     * DB가 데이터를 필터링 해서 반환하는데, 필터링 됐음을 유저의 MBTI로 구분한다.
     * ISTJ -> 필터링 되지 않고 그대로 받환되는 데이터의 MBTI
     * ENFP -> 필터링 되어 받을 수 없는 데이터의 MBTI
     * */
    User survivedUser;
    Mbti survivedMBTI = Mbti.ISTJ;
    List<MBTIFactor> survivedMBTIFactors = List.of(
            MBTIFactor.I, MBTIFactor.S, MBTIFactor.T, MBTIFactor.J
    );
    User filteredUser;
    Mbti filteredMBTI = Mbti.ENFP;
    List<MBTIFactor> filteredMBTIFactors = List.of(
            MBTIFactor.E, MBTIFactor.N, MBTIFactor.F, MBTIFactor.P
    );

    LocalDate startDate, endDate;

    /**
     * JPA에서 등록된 엔티티를 테이블로 생성 해주지만, 뷰는 생성 안되서, sql 파일로 직접 생성
     * */
    @BeforeAll
    public static void createView(@Autowired DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("view/before-test-schema.sql"));
        populator.execute(dataSource);
    }

    /**
     * 필터링 결과를 구분하기 위해, 필터링 대상 MBTI를 가지고 있는 유저 객체 2개를 생성한다.
     * */
    @BeforeEach
    public void createTwoUserToDivideFilteringResultAndLocalDateRange() {
        survivedUser = User.builder()
                .email("survived@email")
                .authenticationName("survivedUser")
                .mbti(survivedMBTI)
                .gender(Gender.MALE)
                .build();
        filteredUser = User.builder()
                .email("filtered@email")
                .authenticationName("filteredUser")
                .mbti(filteredMBTI) // 정반대의 MBTI 입력 -> 이 MBTI는 필터링 되는 MBTI
                .gender(Gender.FEMALE)
                .build();
        userRepository.save(survivedUser);
        userRepository.save(filteredUser);

        startDate = LocalDate.now().minusDays(2);
        endDate = LocalDate.now().minusDays(1);
    }

    @Nested
    class getAmountAveragesEachMBTIAndEmotionBetweenStartDateAndEndDate {
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
                                    .spendDate(endDate.atStartOfDay().plusSeconds(1L))
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
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<MBTIEmotionAmountAverageDto> dtos = mbtiStatisticsRepository.getAmountAveragesEachMBTIAndEmotionBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );
            assertThat(articleRepository.findAll()).hasSize(4);

            // then
            assertThat(dtos.stream()
                    .map(MBTIEmotionAmountAverageDto::getMbtiFactor)
                    .distinct()
                    .toList())
                    .containsExactlyInAnyOrderElementsOf(survivedMBTIFactors)
                    .doesNotContainAnyElementsOf(filteredMBTIFactors);
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
            List<MBTIEmotionAmountAverageDto> dtos = mbtiStatisticsRepository.getAmountAveragesEachMBTIAndEmotionBetweenStartDateAndEndDate(
                    survivedRegisterType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos.stream()
                    .map(MBTIEmotionAmountAverageDto::getMbtiFactor)
                    .distinct()
                    .toList())
                    .containsExactlyInAnyOrderElementsOf(survivedMBTIFactors)
                    .doesNotContainAnyElementsOf(filteredMBTIFactors);
        }

        @Test
        @Order(1)
        void MBTI별_감정별_금액의_평균을_반환한다() throws Exception {
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
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(2000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(3000)
                                    .build()
                                    .createArticle()));
            Long amountAverage = (long) articles.stream()
                    .map(Article::getAmount)
                    .reduce(Integer::sum)
                    .get() / articles.size();
            amountAverage = roundingAverage(amountAverage);

            em.flush();

            // when
            List<MBTIEmotionAmountAverageDto> dtos = mbtiStatisticsRepository.getAmountAveragesEachMBTIAndEmotionBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos)
                    .extracting(MBTIEmotionAmountAverageDto::getEmotion)
                    .containsOnly(emotion);
            assertThat(dtos)
                    .extracting(MBTIEmotionAmountAverageDto::getAmountAverage)
                    .containsOnly(amountAverage);
        }

        @Test
        @Order(2)
        void 금액의_평균을_반활할_때_100의_자리수에서_반올림_한다() throws Exception {
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
                                    .amount(1100)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(2200)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(3300)
                                    .build()
                                    .createArticle()));
            Long amountAverage = (long) articles.stream()
                    .map(Article::getAmount)
                    .reduce(Integer::sum)
                    .get() / articles.size();
            amountAverage = roundingAverage(amountAverage);

            em.flush();

            // when
            List<MBTIEmotionAmountAverageDto> dtos = mbtiStatisticsRepository.getAmountAveragesEachMBTIAndEmotionBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos)
                    .extracting(MBTIEmotionAmountAverageDto::getAmountAverage)
                    .containsOnly(amountAverage);
            assertThat(amountAverage % 1000).isZero();
        }
    }

    @Nested
    class getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate {
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
                                    .spendDate(endDate.atStartOfDay().plusSeconds(1L))
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
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<MBTIDailyAmountSumDto> dtos = mbtiStatisticsRepository.getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos.stream()
                    .map(MBTIDailyAmountSumDto::getMbtiFactor)
                    .distinct()
                    .toList())
                    .containsExactlyInAnyOrderElementsOf(survivedMBTIFactors)
                    .doesNotContainAnyElementsOf(filteredMBTIFactors);
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
            List<MBTIDailyAmountSumDto> dtos = mbtiStatisticsRepository.getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(
                    survivedRegisterType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos.stream()
                    .map(MBTIDailyAmountSumDto::getMbtiFactor)
                    .distinct()
                    .toList())
                    .containsExactlyInAnyOrderElementsOf(survivedMBTIFactors)
                    .doesNotContainAnyElementsOf(filteredMBTIFactors);
        }

        @Test
        void MBTI별_일별_금액의_합을_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            Emotion emotion = Emotion.PROUD;
            List<Article> articles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(2000)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .emotion(emotion)
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(3000)
                                    .build()
                                    .createArticle()));
            long sum = articles.stream()
                    .mapToLong(Article::getAmount)
                    .reduce(Long::sum).orElse(0L);
            em.flush();

            // when
            List<MBTIDailyAmountSumDto> dtos = mbtiStatisticsRepository.getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos)
                    .extracting(MBTIDailyAmountSumDto::getAmountSum)
                    .containsOnly(sum);
        }
    }
    @Nested
    class getAllMemosByMBTIBetweenStartDateAndEndDate {
        @Test
        void 입력받은_registerType을_기준으로_데이터를_필터링해서_반환한다() throws Exception {
            // given
            RegisterType filteredResiterType = RegisterType.SAVE;
            RegisterType survivedRegisterType = RegisterType.SPEND;
            String survivedContent = "survivedContent";
            String filteredContent = "filteredContent";
            List<Article> filteredArticles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(filteredResiterType)
                                    .spendDate(startDate.atStartOfDay())
                                    .content(filteredContent)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(filteredResiterType)
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
            List<MemoDto> dtos = mbtiStatisticsRepository.getAllMemosByMBTIBetweenStartDateAndEndDate(
                    survivedRegisterType,
                    Mbti.NONE.toString(),
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
            List<MemoDto> dtos = mbtiStatisticsRepository.getAllMemosByMBTIBetweenStartDateAndEndDate(
                    registerType,
                    Mbti.NONE.toString(),
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
        void MBTI_파라미터에_해당하는_MBTI_유저들의_메모가_반환된다() throws Exception {
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
            List<MemoDto> dtos = mbtiStatisticsRepository.getAllMemosByMBTIBetweenStartDateAndEndDate(
                    registerType,
                    survivedUser.getMbti().toString(),
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
        void MBTI_파라미터로_NONE을_입력하면_모든_유저들의_메모가_반환된다() throws Exception {
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
            List<MemoDto> dtos = mbtiStatisticsRepository.getAllMemosByMBTIBetweenStartDateAndEndDate(
                    registerType,
                    Mbti.NONE.toString(),
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos)
                    .extracting(MemoDto::getContent)
                    .containsAnyElementsOf(
                            filteredArticles.stream()
                                    .map(Article::getContent)
                                    .toList())
                    .containsAnyElementsOf(
                            survivedArticles.stream()
                                    .map(Article::getContent)
                                    .toList());
        }

    }

    @Nested
    class getSatisfactionAveragesEachMBTIBetweenStartDateAndEndDate {
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
                                    .spendDate(endDate.atStartOfDay().plusSeconds(1L))
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
                                    .spendDate(endDate.atStartOfDay())
                                    .amount(1000)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<MBTISatisfactionAverageDto> dtos = mbtiStatisticsRepository.getSatisfactionAveragesEachMBTIBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos.stream()
                    .map(MBTISatisfactionAverageDto::getMbtiFactor)
                    .distinct()
                    .toList())
                    .containsExactlyInAnyOrderElementsOf(survivedMBTIFactors)
                    .doesNotContainAnyElementsOf(filteredMBTIFactors);
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
            List<MBTISatisfactionAverageDto> dtos = mbtiStatisticsRepository.getSatisfactionAveragesEachMBTIBetweenStartDateAndEndDate(
                    survivedRegisterType,
                    startDate,
                    endDate
            );

            // then
            assertThat(dtos.stream()
                    .map(MBTISatisfactionAverageDto::getMbtiFactor)
                    .distinct()
                    .toList())
                    .containsExactlyInAnyOrderElementsOf(survivedMBTIFactors)
                    .doesNotContainAnyElementsOf(filteredMBTIFactors);
        }

        @Test
        @Order(1)
        void MBTI별_만족도의_평균을_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            List<Article> articles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay())
                                    .satisfaction(3.0f)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay())
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
            List<MBTISatisfactionAverageDto> dtos = mbtiStatisticsRepository.getSatisfactionAveragesEachMBTIBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(
                    dtos.stream()
                            .map(MBTISatisfactionAverageDto::getSatisfactionAverage)
                            .toList()
            ).containsOnly(satisfactionAverage);
        }

        @Test
        @Order(2)
        void 만족도의_평균을_소수_둘째_자리에서_반올림해서_반환한다() throws Exception {
            // given
            RegisterType registerType = RegisterType.SPEND;
            List<Article> articles = List.of(
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay())
                                    .satisfaction(3.04f)
                                    .build()
                                    .createArticle()),
                    articleRepository.save(
                            ArticleFactory.builder()
                                    .user(survivedUser)
                                    .registerType(registerType)
                                    .spendDate(endDate.atStartOfDay())
                                    .satisfaction(4.08f)
                                    .build()
                                    .createArticle()));
            em.flush();

            // when
            List<MBTISatisfactionAverageDto> dtos = mbtiStatisticsRepository.getSatisfactionAveragesEachMBTIBetweenStartDateAndEndDate(
                    registerType,
                    startDate,
                    endDate
            );

            // then
            assertThat(
                    dtos.stream()
                            .map(MBTISatisfactionAverageDto::getSatisfactionAverage)
                            .toList()
            ).containsOnly(3.6f);
        }
    }

    /**
     * Long 타입 변수를 받아 100의 자리에서 반올림하는 메서드
     * */
    private static Long roundingAverage(Long amountAverage) {
        amountAverage += 500L;
        amountAverage = amountAverage - (amountAverage % 1000);
        return amountAverage;
    }
}