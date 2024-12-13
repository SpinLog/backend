package com.example.spinlog.statistics.repository;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.statistics.dto.*;
import com.example.spinlog.statistics.dto.repository.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.GenderDataDto;
import com.example.spinlog.statistics.dto.repository.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.repository.MBTIDailyAmountSumDto;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import com.example.spinlog.util.ArticleFactory;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class MBTIStatisticsRepositoryQueryTest extends MBTIStatisticsRepositoryTestSupport {
    @Autowired
    MBTIStatisticsRepository mbtiStatisticsRepository;
    @Autowired
    SpecificUserStatisticsRepository specificUserStatisticsRepository;
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
    static void setUp(@Autowired UserRepository userRepository, @Autowired ArticleRepository articleRepository) {
        user1 = User.builder()
                .email("survived@email")
                .authenticationName("survivedUser")
                .mbti(Mbti.ISTJ)
                .gender(Gender.MALE)
                .build();
        user2 = User.builder()
                .email("filtered@email")
                .authenticationName("filteredUser")
                .mbti(Mbti.ENFP)
                .gender(Gender.FEMALE)
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
    class getAmountSumsAndCountsEachMBTIAndEmotionBetweenStartDateAndEndDate {
        @Test
        void MBTI_별로_그룹핑한다() throws Exception {
            // when
            List<MBTIEmotionAmountSumAndCountDto> dtos =
                    mbtiStatisticsRepository.getAmountSumsAndCountsEachMBTIAndEmotionBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(MBTIEmotionAmountSumAndCountDto::getMbtiFactor).toList(),
                    MBTIFactor.values());
        }

        @Test
        void 감정별로_그룹핑한다() throws Exception {
            // when
            List<MBTIEmotionAmountSumAndCountDto> dtos =
                    mbtiStatisticsRepository.getAmountSumsAndCountsEachMBTIAndEmotionBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(MBTIEmotionAmountSumAndCountDto::getEmotion).toList(),
                    Emotion.PROUD, Emotion.SAD);
        }

        @Test
        void 필터링_이후_각_amount_의_합을_반환한다() throws Exception {
            // when
            List<MBTIEmotionAmountSumAndCountDto> dtos =
                    mbtiStatisticsRepository.getAmountSumsAndCountsEachMBTIAndEmotionBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            var sadStream = dtos.stream().filter(dto -> dto.getEmotion().equals(Emotion.SAD));

            assertThat(sadStream
                    .map(MBTIEmotionAmountSumAndCountDto::getAmountSum)
                    .findAny()
                    .orElseThrow()).isEqualTo(11L);

            var proudStream = dtos.stream().filter(dto -> dto.getEmotion().equals(Emotion.PROUD));

            assertThat(proudStream
                    .map(MBTIEmotionAmountSumAndCountDto::getAmountSum)
                    .findAny()
                    .orElseThrow()).isEqualTo(1100L);

            assertThat(dtos
                    .stream()
                    .map(MBTIEmotionAmountSumAndCountDto::getAmountCount)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(2L);

        }
    }

    @Nested
    class getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate {
        @Test
        void MBTI_별로_그룹핑한다() throws Exception {
            // when
            List<MBTIDailyAmountSumDto> dtos =
                    mbtiStatisticsRepository.getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(MBTIDailyAmountSumDto::getMbtiFactor).toList(),
                    MBTIFactor.values());
        }

        @Test
        void 일별로_그룹핑한다() throws Exception {
            // when
            List<MBTIDailyAmountSumDto> dtos =
                    mbtiStatisticsRepository.getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(MBTIDailyAmountSumDto::getLocalDate).toList(),
                    startDate, endDate.minusDays(1));
        }

        @Test
        void 필터링_이후_각_amount_의_합를_반환한다() throws Exception {
            // when
            List<MBTIDailyAmountSumDto> dtos =
                    mbtiStatisticsRepository.getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            assertThat(dtos.stream()
                    .filter(dto -> dto.getLocalDate().equals(endDate.minusDays(1)))
                    .map(MBTIDailyAmountSumDto::getAmountSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(11L);

            assertThat(dtos.stream()
                    .filter(dto -> dto.getLocalDate().equals(startDate))
                    .map(MBTIDailyAmountSumDto::getAmountSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(1100L);
        }
    }

    @Nested
    class getSatisfactionSumsAndCountsEachMBTIBetweenStartDateAndEndDate {
        @Test
        void MBTI_별로_그룹핑한다() throws Exception {
            // when
            List<MBTISatisfactionSumAndCountDto> dtos =
                    mbtiStatisticsRepository.getSatisfactionSumsAndCountsEachMBTIBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(MBTISatisfactionSumAndCountDto::getMbtiFactor).toList(),
                    MBTIFactor.values());
        }

        @Test
        void 필터링_이후_각_satisfaction_의_합과_개수를_반환한다() throws Exception {
            // when
            List<MBTISatisfactionSumAndCountDto> dtos =
                    mbtiStatisticsRepository.getSatisfactionSumsAndCountsEachMBTIBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            assertThat(dtos.stream()
                    .map(MBTISatisfactionSumAndCountDto::getSatisfactionSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(14.0);

            assertThat(dtos.stream()
                    .map(MBTISatisfactionSumAndCountDto::getSatisfactionCount)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(4L);
        }
    }

    @Nested
    class getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate {
        @Test
        void 감정별로_그룹핑한다() throws Exception {
            // when
            List<EmotionAmountSumAndCountDto> dtos =
                    specificUserStatisticsRepository.getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(EmotionAmountSumAndCountDto::getEmotion).toList(),
                    Emotion.PROUD, Emotion.SAD);
        }

        @Test
        void 필터링_이후_각_amount_의_합을_반환한다() throws Exception {
            // when
            List<EmotionAmountSumAndCountDto> dtos =
                    specificUserStatisticsRepository.getAmountSumsAndCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);
            // then
            var sadStream = dtos.stream().filter(dto -> dto.getEmotion().equals(Emotion.SAD));

            assertThat(sadStream
                    .map(EmotionAmountSumAndCountDto::getAmountSum)
                    .findAny()
                    .orElseThrow()).isEqualTo(11L);

            var proudStream = dtos.stream().filter(dto -> dto.getEmotion().equals(Emotion.PROUD));

            assertThat(proudStream
                    .map(EmotionAmountSumAndCountDto::getAmountSum)
                    .findAny()
                    .orElseThrow()).isEqualTo(1100L);

            assertThat(dtos
                    .stream()
                    .map(EmotionAmountSumAndCountDto::getAmountCount)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(2L);
        }
    }

    @Nested
    class getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate {@Test
        void 일별로_그룹핑한다() throws Exception {
            // when
            List<DailyAmountSumDto> dtos =
                    specificUserStatisticsRepository.getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(DailyAmountSumDto::getLocalDate).toList(),
                    startDate, endDate.minusDays(1));
        }

        @Test
        void 필터링_이후_각_amount_의_합를_반환한다() throws Exception {
            // when
            List<DailyAmountSumDto> dtos =
                    specificUserStatisticsRepository.getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);

            // then
            assertThat(dtos.stream()
                    .filter(dto -> dto.getLocalDate().equals(endDate.minusDays(1)))
                    .map(DailyAmountSumDto::getAmountSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(11L);

            assertThat(dtos.stream()
                    .filter(dto -> dto.getLocalDate().equals(startDate))
                    .map(DailyAmountSumDto::getAmountSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(1100L);
        }
    }

    @Nested
    class getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate {
        @Test
        void 필터링_이후_각_satisfaction_의_합과_개수를_반환한다() throws Exception {
            // when
            List<SatisfactionSumAndCountDto> dtos =
                    specificUserStatisticsRepository.getSatisfactionSumsAndCountsByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);

            // then
            assertThat(dtos.stream()
                    .map(SatisfactionSumAndCountDto::getSatisfactionSum)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(14.0);

            assertThat(dtos.stream()
                    .map(SatisfactionSumAndCountDto::getSatisfactionCount)
                    .findAny()
                    .orElseThrow())
                    .isEqualTo(4L);
        }
    }
}
