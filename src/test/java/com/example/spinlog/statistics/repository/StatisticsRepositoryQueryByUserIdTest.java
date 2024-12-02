package com.example.spinlog.statistics.repository;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.statistics.dto.*;
import com.example.spinlog.statistics.dto.repository.MBTIDailyAmountSumDto;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import com.example.spinlog.util.ArticleFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class StatisticsRepositoryQueryByUserIdTest {
    @Autowired
    MBTIStatisticsRepository mbtiStatisticsRepository;
    @Autowired
    GenderStatisticsRepository genderStatisticsRepository; // ByUserId 쿼리 중복 제거
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserRepository userRepository;

    static LocalDate startDate = LocalDate.of(2024, 8, 27);
    static LocalDate endDate = LocalDate.of(2024, 8, 30);

    static Emotion filteredEmotion = Emotion.ANNOYED;
    static User user1;
    static User user2;

    @BeforeAll
    static void setUp(@Autowired DataSource dataSource, @Autowired UserRepository userRepository, @Autowired ArticleRepository articleRepository) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("view/before-test-schema.sql"));
        populator.execute(dataSource);

        user1 = User.builder()
                .email("survived@email")
                .authenticationName("survivedUser")
                .mbti(Mbti.ISTJ)
                .gender(Gender.MALE)
                .build();
        user2 = User.builder()
                .email("filtered@email")
                .authenticationName("filteredUser")
                .mbti(Mbti.ISTJ)
                .gender(Gender.MALE)
                .build();
        userRepository.save(user1);
        userRepository.save(user2);

        List<Emotion> emotions = List.of(
                Emotion.PROUD, Emotion.PROUD,
                Emotion.SAD, Emotion.SAD,
                Emotion.PROUD, Emotion.PROUD,
                Emotion.SAD, Emotion.SAD
        );
        List<Integer> amounts = List.of(
                1000, 100,
                10, 1,
                1000, 100,
                10, 1
        );
        List<Float> satisfactions = List.of(
                5.0f, 4.0f,
                3.0f, 2.0f,
                5.0f, 4.0f,
                3.0f, 2.0f
        );
        List<LocalDateTime> spendDates = List.of(
                startDate.atStartOfDay(), startDate.atStartOfDay(),
                endDate.atStartOfDay().minusSeconds(1), endDate.atStartOfDay().minusSeconds(1),
                startDate.atStartOfDay(), startDate.atStartOfDay(),
                endDate.atStartOfDay().minusSeconds(1), endDate.atStartOfDay().minusSeconds(1)
        );
        List<User> users = List.of(
                user1, user1,
                user1, user1,
                user2, user2,
                user2, user2
        );

        for (int i = 0; i < emotions.size(); i++) {
            articleRepository.save(
                    ArticleFactory.builder()
                            .emotion(emotions.get(i))
                            .amount(amounts.get(i))
                            .satisfaction(satisfactions.get(i))
                            .spendDate(spendDates.get(i))
                            .user(users.get(i))
                            .registerType(RegisterType.SPEND)
                            .build()
                            .createArticle());
        }

        // ---- save filtered articles ----
        articleRepository.save(
                ArticleFactory.builder()
                        .emotion(filteredEmotion)
                        .spendDate(startDate.atStartOfDay())
                        .user(user2)
                        .registerType(RegisterType.SAVE)
                        .build()
                        .createArticle());
        articleRepository.save(
                ArticleFactory.builder()
                        .emotion(filteredEmotion)
                        .spendDate(startDate.atStartOfDay().minusSeconds(1))
                        .user(user2)
                        .registerType(RegisterType.SPEND)
                        .build()
                        .createArticle());
        articleRepository.save(
                ArticleFactory.builder()
                        .emotion(filteredEmotion)
                        .spendDate(endDate.atStartOfDay())
                        .user(user2)
                        .registerType(RegisterType.SPEND)
                        .build()
                        .createArticle());
    }

    @AfterAll
    static void tearDown(@Autowired UserRepository userRepository, @Autowired ArticleRepository articleRepository) {
        articleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @SafeVarargs
    private <T, LU extends List<U>, U> void checkContainsOnly(T target, Function<T, LU> function, U... result) {
        assertThat(function.apply(target)).containsOnly(result);
    }

    private <T, U> void checkIsEqualTo(T target, Function<T, U> function, U result) {
        assertThat(function.apply(target)).isEqualTo(result);
    }

    @Nested
    class getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate_특정_유저에_대한_감정별_금액_합과_개수를_반환하는_메서드 {
        @Test
        void 특정_유저의_정보만_가져온다() throws Exception {
            // when
            List<EmotionAmountSumAndCountDto> specificUserDto =
                    mbtiStatisticsRepository.getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);
            List<MBTIEmotionAmountSumAndCountDto> allUsersDtos = mbtiStatisticsRepository.getAmountSumsAndCountsEachMBTIAndEmotionBetweenStartDateAndEndDate(
                    RegisterType.SPEND, startDate, endDate);

            // then
            assertThat(specificUserDto.stream()
                    .filter(dto -> dto.getEmotion().equals(Emotion.PROUD))
                    .map(EmotionAmountSumAndCountDto::getAmountSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(1100L);
            assertThat(specificUserDto.stream()
                    .filter(dto -> dto.getEmotion().equals(Emotion.SAD))
                    .map(EmotionAmountSumAndCountDto::getAmountSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(11L);

            assertThat(allUsersDtos.stream()
                    .filter(dto -> dto.getEmotion().equals(Emotion.PROUD))
                    .map(MBTIEmotionAmountSumAndCountDto::getAmountSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(2200L);
            assertThat(allUsersDtos.stream()
                    .filter(dto -> dto.getEmotion().equals(Emotion.SAD))
                    .map(MBTIEmotionAmountSumAndCountDto::getAmountSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(22L);
        }
    }

    @Nested
    class getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate_특정_유저에_대한_일별_금액_합을_반환하는_메서드 {
        @Test
        void 특정_유저의_정보만_가져온다() throws Exception {
            // when
            List<DailyAmountSumDto> specificUserDto =
                    mbtiStatisticsRepository.getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);
            List<MBTIDailyAmountSumDto> allUsersDtos = mbtiStatisticsRepository.getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(
                    RegisterType.SPEND, startDate, endDate);

            // then
            assertThat(specificUserDto.stream()
                    .filter(dto -> dto.getLocalDate().equals(startDate))
                    .map(DailyAmountSumDto::getAmountSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(1100L);
            assertThat(specificUserDto.stream()
                    .filter(dto -> dto.getLocalDate().equals(endDate.minusDays(1)))
                    .map(DailyAmountSumDto::getAmountSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(11L);

            assertThat(allUsersDtos.stream()
                    .filter(dto -> dto.getLocalDate().equals(startDate))
                    .map(MBTIDailyAmountSumDto::getAmountSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(2200L);
            assertThat(allUsersDtos.stream()
                    .filter(dto -> dto.getLocalDate().equals(endDate.minusDays(1)))
                    .map(MBTIDailyAmountSumDto::getAmountSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(22L);
        }
    }

    @Nested
    class getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate_특정_유저에_대한_만족도_합과_개수를_반환하는_메서드 {
        @Test
        void 특정_유저의_정보만_가져온다() throws Exception {
            // when
            List<SatisfactionSumAndCountDto> specificUserDto =
                    mbtiStatisticsRepository.getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);
            List<MBTISatisfactionSumAndCountDto> allUsersDtos = mbtiStatisticsRepository.getSatisfactionSumsAndCountsEachMBTIBetweenStartDateAndEndDate(
                    RegisterType.SPEND, startDate, endDate);

            // then
            assertThat(specificUserDto.stream()
                    .map(SatisfactionSumAndCountDto::getSatisfactionCount)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(4L);

            assertThat(allUsersDtos.stream()
                    .map(MBTISatisfactionSumAndCountDto::getSatisfactionCount)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(8L);
        }
    }
}
