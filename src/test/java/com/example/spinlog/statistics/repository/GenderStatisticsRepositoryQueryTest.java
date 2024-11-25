package com.example.spinlog.statistics.repository;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.statistics.dto.repository.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.GenderDataDto;
import com.example.spinlog.statistics.dto.repository.GenderEmotionAmountAverageDto;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import com.example.spinlog.util.ArticleFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsRepositoryQueryTest {
    @Autowired GenderStatisticsRepository genderStatisticsRepository;
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
    class getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate_성별_감정별_금액_합을_반환하는_메서드 {
        @Test
        void 성별로_그룹핑한다() throws Exception {
            // when
            List<GenderEmotionAmountAverageDto> dtos =
                    genderStatisticsRepository.getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(GenderEmotionAmountAverageDto::getGender).toList(),
                    Gender.MALE, Gender.FEMALE);
        }
        
        @Test
        void 감정별로_그룹핑한다() throws Exception {
            // when
            List<GenderEmotionAmountAverageDto> dtos =
                    genderStatisticsRepository.getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);
            
            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(GenderEmotionAmountAverageDto::getEmotion).toList(),
                    Emotion.PROUD, Emotion.SAD);
        }

        @Test
        void 필터링_이후_각_amount_의_합을_반환한다() throws Exception {
            // when
            List<GenderEmotionAmountAverageDto> dtos =
                    genderStatisticsRepository.getAmountSumsEachGenderAndEmotionBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            List<Emotion> emotions = List.of(Emotion.PROUD, Emotion.PROUD, Emotion.SAD, Emotion.SAD);
            List<Gender> genders = List.of(Gender.MALE, Gender.FEMALE, Gender.MALE, Gender.FEMALE);
            List<Long> amountSums = List.of(1100L, 1100L, 11L, 11L);
            for (int i = 0; i < dtos.size(); i++) {
                final int index = i;
                checkIsEqualTo(
                        dtos,
                        (dto) -> dto.stream()
                                .filter(d ->
                                        d.getGender().equals(genders.get(index)) &&
                                                d.getEmotion().equals(emotions.get(index)))
                                .map(GenderEmotionAmountAverageDto::getAmountAverage)
                                .findFirst().orElseThrow(),
                        amountSums.get(index));
            }
        }
    }

    @Nested
    class getAmountCountsEachGenderAndEmotionBetweenStartDateAndEndDate_성별_감정별_금액_개수를_반환하는_메서드 {
        @Test
        void 성별로_그룹핑한다() throws Exception {
            // when
            List<GenderEmotionAmountAverageDto> dtos =
                    genderStatisticsRepository.getAmountCountsEachGenderAndEmotionBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(GenderEmotionAmountAverageDto::getGender).toList(),
                    Gender.MALE, Gender.FEMALE);
        }

        @Test
        void 감정별로_그룹핑한다() throws Exception {
            // when
            List<GenderEmotionAmountAverageDto> dtos =
                    genderStatisticsRepository.getAmountCountsEachGenderAndEmotionBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(GenderEmotionAmountAverageDto::getEmotion).toList(),
                    Emotion.PROUD, Emotion.SAD);
        }

        @Test
        void 필터링_이후_각_amount_의_개수를_반환한다() throws Exception {
            // when
            List<GenderEmotionAmountAverageDto> dtos =
                    genderStatisticsRepository.getAmountCountsEachGenderAndEmotionBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            List<Emotion> emotions = List.of(Emotion.PROUD, Emotion.PROUD, Emotion.SAD, Emotion.SAD);
            List<Gender> genders = List.of(Gender.MALE, Gender.FEMALE, Gender.MALE, Gender.FEMALE);
            List<Long> amountSums = List.of(2L, 2L, 2L, 2L);
            for (int i = 0; i < dtos.size(); i++) {
                final int index = i;
                checkIsEqualTo(
                        dtos,
                        (dto) -> dto.stream()
                                .filter(d ->
                                        d.getGender().equals(genders.get(index)) &&
                                                d.getEmotion().equals(emotions.get(index)))
                                .map(GenderEmotionAmountAverageDto::getAmountAverage)
                                .findFirst().orElseThrow(),
                        amountSums.get(index));
            }
        }
    }

    @Nested
    class getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate_성별_일별_금액_합을_반환하는_메서드 {
        @Test
        void 성별로_그룹핑한다() throws Exception {
            // when
            List<GenderDailyAmountSumDto> dtos =
                    genderStatisticsRepository.getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(GenderDailyAmountSumDto::getGender).toList(),
                    Gender.MALE, Gender.FEMALE);
        }

        @Test
        void 일별로_그룹핑한다() throws Exception {
            // when
            List<GenderDailyAmountSumDto> dtos =
                    genderStatisticsRepository.getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(GenderDailyAmountSumDto::getLocalDate).toList(),
                    startDate, endDate.minusDays(1));
        }

        @Test
        void 필터링_이후_각_amount_의_합를_반환한다() throws Exception {
            // when
            List<GenderDailyAmountSumDto> dtos =
                    genderStatisticsRepository.getAmountSumsEachGenderAndDayBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            List<LocalDate> dates =
                    List.of(startDate, startDate, endDate.minusDays(1), endDate.minusDays(1));
            List<Gender> genders = List.of(Gender.MALE, Gender.FEMALE, Gender.MALE, Gender.FEMALE);
            List<Long> amountSums = List.of(1100L, 1100L, 11L, 11L);
            for (int i = 0; i < dtos.size(); i++) {
                final int index = i;
                checkIsEqualTo(
                        dtos,
                        (dto) -> dto.stream()
                                .filter(d ->
                                        d.getGender().equals(genders.get(index)) &&
                                                d.getLocalDate().equals(dates.get(index)))
                                .map(GenderDailyAmountSumDto::getAmountSum)
                                .findFirst().orElseThrow(),
                        amountSums.get(index));
            }
        }
    }

    @Nested
    class getSatisfactionSumsEachGenderBetweenStartDateAndEndDate_성별_만족도_합을_반환하는_메서드 {
        @Test
        void 성별로_그룹핑한다() throws Exception {
            // when
            List<GenderDataDto<Double>> dtos =
                    genderStatisticsRepository.getSatisfactionSumsEachGenderBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(GenderDataDto::getGender).toList(),
                    Gender.MALE, Gender.FEMALE);
        }

        @Test
        void 필터링_이후_각_satisfaction_의_합를_반환한다() throws Exception {
            // when
            List<GenderDataDto<Double>> dtos =
                    genderStatisticsRepository.getSatisfactionSumsEachGenderBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            List<Gender> genders = List.of(Gender.MALE, Gender.FEMALE);
            List<Double> satisfactionSums = List.of(14.0, 14.0);
            for (int i = 0; i < dtos.size(); i++) {
                final int index = i;
                checkIsEqualTo(
                        dtos,
                        (dto) -> dto.stream()
                                .filter(d ->
                                        d.getGender().equals(genders.get(index)))
                                .map(GenderDataDto::getValue)
                                .findFirst().orElseThrow(),
                        satisfactionSums.get(index));
            }
        }
    }

    @Nested
    class getSatisfactionCountsEachGenderBetweenStartDateAndEndDate_성별_만족도_개수를_반환하는_메서드 {
        @Test
        void 성별로_그룹핑한다() throws Exception {
            // when
            List<GenderDataDto<Long>> dtos =
                    genderStatisticsRepository.getSatisfactionCountsEachGenderBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(GenderDataDto::getGender).toList(),
                    Gender.MALE, Gender.FEMALE);
        }

        @Test
        void 필터링_이후_각_satisfaction_의_개수를_반환한다() throws Exception {
            // when
            List<GenderDataDto<Long>> dtos =
                    genderStatisticsRepository.getSatisfactionCountsEachGenderBetweenStartDateAndEndDate(
                            RegisterType.SPEND, startDate, endDate);

            // then
            List<Gender> genders = List.of(Gender.MALE, Gender.FEMALE);
            List<Long> satisfactionCounts = List.of(4L, 4L);
            for (int i = 0; i < dtos.size(); i++) {
                final int index = i;
                checkIsEqualTo(
                        dtos,
                        (dto) -> dto.stream()
                                .filter(d ->
                                        d.getGender().equals(genders.get(index)))
                                .map(GenderDataDto::getValue)
                                .findFirst().orElseThrow(),
                        satisfactionCounts.get(index));
            }
        }
    }

    @Nested
    class getAmountSumsEachEmotionByUserIdBetweenStartDateAndEndDate_특정_유저에_대한_감정별_금액_합을_반환하는_메서드 {
        @Test
        void 감정별로_그룹핑한다() throws Exception {
            // when
            List<GenderEmotionAmountAverageDto> dtos =
                    genderStatisticsRepository.getAmountSumsEachEmotionByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(GenderEmotionAmountAverageDto::getEmotion).toList(),
                    Emotion.PROUD, Emotion.SAD);
        }

        @Test
        void 필터링_이후_각_amount_의_합을_반환한다() throws Exception {
            // when
            List<GenderEmotionAmountAverageDto> dtos =
                    genderStatisticsRepository.getAmountSumsEachEmotionByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);

            // then
            List<Emotion> emotions = List.of(Emotion.PROUD, Emotion.SAD);
            List<Long> amountSums = List.of(1100L, 11L);
            for (int i = 0; i < dtos.size(); i++) {
                final int index = i;
                checkIsEqualTo(
                        dtos,
                        (dto) -> dto.stream()
                                .filter(d -> d.getEmotion().equals(emotions.get(index)))
                                .map(GenderEmotionAmountAverageDto::getAmountAverage)
                                .findFirst().orElseThrow(),
                        amountSums.get(index));
            }
        }
    }

    @Nested
    class getAmountCountsEachEmotionByUserIdBetweenStartDateAndEndDate_특정_유저에_대한_감정별_금액_개수를_반환하는_메서드 {
        @Test
        void 감정별로_그룹핑한다() throws Exception {
            // when
            List<GenderEmotionAmountAverageDto> dtos =
                    genderStatisticsRepository.getAmountCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(GenderEmotionAmountAverageDto::getEmotion).toList(),
                    Emotion.PROUD, Emotion.SAD);
        }

        @Test
        void 필터링_이후_각_amount_의_개수를_반환한다() throws Exception {
            // when
            List<GenderEmotionAmountAverageDto> dtos =
                    genderStatisticsRepository.getAmountCountsEachEmotionByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);

            // then
            List<Emotion> emotions = List.of(Emotion.PROUD, Emotion.SAD);
            List<Long> amountCounts = List.of(2L, 2L);
            for (int i = 0; i < dtos.size(); i++) {
                final int index = i;
                checkIsEqualTo(
                        dtos,
                        (dto) -> dto.stream()
                                .filter(d -> d.getEmotion().equals(emotions.get(index)))
                                .map(GenderEmotionAmountAverageDto::getAmountAverage)
                                .findFirst().orElseThrow(),
                        amountCounts.get(index));
            }
        }
    }

    @Nested
    class getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate_특정_유저에_대한_일별_금액_합을_반환하는_메서드 {
        @Test
        void 일별로_그룹핑한다() throws Exception {
            // when
            List<GenderDailyAmountSumDto> dtos =
                    genderStatisticsRepository.getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);

            // then
            checkContainsOnly(
                    dtos, (dto) -> dto.stream().map(GenderDailyAmountSumDto::getLocalDate).toList(),
                    startDate, endDate.minusDays(1));
        }

        @Test
        void 필터링_이후_각_amount_의_합를_반환한다() throws Exception {
            // when
            List<GenderDailyAmountSumDto> dtos =
                    genderStatisticsRepository.getAmountSumsEachDayByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);

            // then
            List<LocalDate> dates =
                    List.of(startDate, endDate.minusDays(1));
            List<Long> amountSums = List.of(1100L, 11L);
            for (int i = 0; i < dtos.size(); i++) {
                final int index = i;
                checkIsEqualTo(
                        dtos,
                        (dto) -> dto.stream()
                                .filter(d -> d.getLocalDate().equals(dates.get(index)))
                                .map(GenderDailyAmountSumDto::getAmountSum)
                                .findFirst().orElseThrow(),
                        amountSums.get(index));
            }
        }
    }

    @Nested
    class getSatisfactionSumsByUserIdBetweenStartDateAndEndDate_특정_유저에_대한_만족도_합을_반환하는_메서드 {
        @Test
        void 필터링_이후_각_satisfaction_의_합를_반환한다() throws Exception {
            // when
            List<GenderDataDto<Double>> dtos =
                    genderStatisticsRepository.getSatisfactionSumsByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);

            // then
            assertThat(dtos.size()).isEqualTo(1);
            assertThat(dtos.get(0).getValue()).isEqualTo(14.0);
        }
    }

    @Nested
    class getSatisfactionCountsByUserIdBetweenStartDateAndEndDate_특정_유저에_대한_만족도_개수를_반환하는_메서드 {
        @Test
        void 필터링_이후_각_satisfaction_의_개수를_반환한다() throws Exception {
            // when
            List<GenderDataDto<Long>> dtos =
                    genderStatisticsRepository.getSatisfactionCountsByUserIdBetweenStartDateAndEndDate(
                            user1.getId(), RegisterType.SPEND, startDate, endDate);

            // then
            assertThat(dtos.size()).isEqualTo(1);
            assertThat(dtos.get(0).getValue()).isEqualTo(4L);
        }
    }

    @Test
    void fdsfsd() throws Exception {
        articleRepository
                .findAll()
                .forEach(a -> System.out.println(
                        a.getUser().getGender() + " "
                                + a.getEmotion() + " "
                                + a.getAmount() + " "
                                + a.getSatisfaction() + " "
                                + a.getRegisterType() + " "
                                + a.getSpendDate() + " "
                ));
        System.out.println(userRepository.findAll());
    }
}