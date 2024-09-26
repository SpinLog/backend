package com.example.spinlog.statistics.service;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.repository.dto.MBTISatisfactionAverageDto;
import com.example.spinlog.statistics.service.dto.*;
import com.example.spinlog.statistics.repository.MBTIStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.MBTIDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.MBTIEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.MemoDto;
import com.example.spinlog.statistics.loginService.AuthenticatedUserService;
import com.example.spinlog.statistics.service.workanalysis.WordExtractionService;
import com.example.spinlog.user.entity.Mbti;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MBTIStatisticsServiceTest {
    @Mock
    WordExtractionService wordExtractionService;
    @Mock
    MBTIStatisticsRepository mbtiStatisticsRepository;
    @Mock
    AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    MBTIStatisticsService statisticsService;

    @Nested
    class getAmountAveragesEachMBTIAndEmotionLast30Days{
        @Test
        void MBTI별_감정별_금액_평균_데이터를_조회한_뒤_MBTI_별로_grouping해서_로그인_한_유저의_MBTI와_함께_반환한다() throws Exception {
            // given
            List<MBTIEmotionAmountAverageDto> returned = List.of(
                    new MBTIEmotionAmountAverageDto(MBTIFactor.I, Emotion.PROUD, 1L),
                    new MBTIEmotionAmountAverageDto(MBTIFactor.I, Emotion.SAD, 2L),
                    new MBTIEmotionAmountAverageDto(MBTIFactor.E, Emotion.PROUD, 3L),
                    new MBTIEmotionAmountAverageDto(MBTIFactor.E, Emotion.SAD, 4L)
            );
            List<Boolean> visited = new ArrayList<>(returned.size());

            when(mbtiStatisticsRepository.getAmountAveragesEachMBTIAndEmotionBetweenStartDateAndEndDate(any(), any(), any()))
                    .thenReturn(returned);
            when(authenticatedUserService.getUserMBTI())
                    .thenReturn(Mbti.ISTJ);

            // when
            MBTIEmotionAmountAverageResponse response =
                    statisticsService.getAmountAveragesEachMBTIAndEmotionLast30Days(null);

            // then
            assertThat(response.getMbti())
                    .isEqualTo(Mbti.ISTJ);

            List<MBTIEmotionAmountAverageResponse.MBTIEmotionAmountAverage> responseList = response.getMbtiEmotionAmountAverages();
            List<MBTIEmotionAmountAverageResponse.MBTIEmotionAmountAverage> responseListWithZeroFiltering =
                    filterNonZeroAndNonEmptyAverages(responseList);

            assertThat(responseListWithZeroFiltering)
                    .hasSize(2);

            List<MBTIFactor> mbtiFactors = returned.stream()
                    .map(MBTIEmotionAmountAverageDto::getMbtiFactor)
                    .distinct()
                    .toList();
            assertThat(responseListWithZeroFiltering)
                    .extracting(MBTIEmotionAmountAverageResponse
                            .MBTIEmotionAmountAverage::getMbtiFactor)
                    .containsExactlyInAnyOrderElementsOf(mbtiFactors);

            for(var r: responseListWithZeroFiltering){
                assertEmotionAmountAverageGroupedByMBTI(r, returned);
            }
        }

        @Test
        void 데이터를_조회한_뒤_zero_padding을_수행한다() throws Exception {
            // given
            List<MBTIEmotionAmountAverageDto> returned = List.of(
                    new MBTIEmotionAmountAverageDto(MBTIFactor.I, Emotion.PROUD, 1L),
                    new MBTIEmotionAmountAverageDto(MBTIFactor.I, Emotion.SAD, 2L),
                    new MBTIEmotionAmountAverageDto(MBTIFactor.E, Emotion.PROUD, 3L),
                    new MBTIEmotionAmountAverageDto(MBTIFactor.E, Emotion.SAD, 4L)
            );
            List<Boolean> visited = new ArrayList<>(returned.size());

            when(mbtiStatisticsRepository.getAmountAveragesEachMBTIAndEmotionBetweenStartDateAndEndDate(any(), any(), any()))
                    .thenReturn(returned);

            // when
            MBTIEmotionAmountAverageResponse response =
                    statisticsService.getAmountAveragesEachMBTIAndEmotionLast30Days(null);

            // then
            List<MBTIEmotionAmountAverageResponse.MBTIEmotionAmountAverage> responseList = response.getMbtiEmotionAmountAverages();

            assertThat(responseList)
                    .extracting(MBTIEmotionAmountAverageResponse.MBTIEmotionAmountAverage::getMbtiFactor)
                    .containsExactlyInAnyOrder(MBTIFactor.values());
            assertThat(responseList)
                    .extracting(MBTIEmotionAmountAverageResponse.MBTIEmotionAmountAverage::getEmotionAmountAverages)
                    .allMatch(list ->
                            list.stream()
                                    .map(MBTIEmotionAmountAverageResponse.EmotionAmountAverage::getEmotion)
                                    .allMatch(Arrays.asList(Emotion.values())::contains));
        }

        private static List<MBTIEmotionAmountAverageResponse.MBTIEmotionAmountAverage> filterNonZeroAndNonEmptyAverages(
                List<MBTIEmotionAmountAverageResponse.MBTIEmotionAmountAverage> responses) {
            return responses.stream()
                    .map(r -> {
                        List<MBTIEmotionAmountAverageResponse.EmotionAmountAverage> list =
                                r.getEmotionAmountAverages().stream()
                                        .filter(ea -> ea.getAmountAverage() != 0)
                                        .toList();
                        return MBTIEmotionAmountAverageResponse
                                .MBTIEmotionAmountAverage.builder()
                                .mbtiFactor(r.getMbtiFactor())
                                .emotionAmountAverages(list)
                                .build();
                    })
                    .filter(r -> !r.getEmotionAmountAverages().isEmpty())
                    .toList();
        }

        private static void assertEmotionAmountAverageGroupedByMBTI(
                MBTIEmotionAmountAverageResponse.MBTIEmotionAmountAverage response,
                List<MBTIEmotionAmountAverageDto> returned) {
            List<Tuple> dailyAmountAveragesGroupedByMBTI = returned.stream()
                    .filter(a -> a.getMbtiFactor()
                            .equals(response.getMbtiFactor()))
                    .map(a -> new Tuple(a.getEmotion(), a.getAmountAverage()))
                    .toList();
            assertThat(response.getEmotionAmountAverages())
                    .extracting("emotion", "amountAverage")
                    .containsExactlyInAnyOrderElementsOf(dailyAmountAveragesGroupedByMBTI);
        }
    }

    @Nested
    class getAmountSumsEachMBTIAndDayLast30Days {
        @Test
        void MBTI별_일별_금액_총합_데이터를_조회한_뒤_MBTI_별로_grouping해서_로그인_한_유저의_MBTI와_함께_반환한다() throws Exception {
            // given
            List<MBTIDailyAmountSumDto> returned = List.of(
                    new MBTIDailyAmountSumDto(MBTIFactor.I, LocalDate.now().minusDays(2L), 1L),
                    new MBTIDailyAmountSumDto(MBTIFactor.I, LocalDate.now().minusDays(1L), 2L),
                    new MBTIDailyAmountSumDto(MBTIFactor.E, LocalDate.now().minusDays(2L), 3L),
                    new MBTIDailyAmountSumDto(MBTIFactor.E, LocalDate.now().minusDays(1L), 4L)
            );
            when(mbtiStatisticsRepository.getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(any(), any(), any()))
                    .thenReturn(returned);
            when(authenticatedUserService.getUserMBTI())
                    .thenReturn(Mbti.ISTJ);

            // when
            MBTIDailyAmountSumResponse response =
                    statisticsService.getAmountSumsEachMBTIAndDayLast30Days(null);

            // then
            assertThat(response.getMbti())
                    .isEqualTo(Mbti.ISTJ);

            List<MBTIDailyAmountSumResponse.MBTIDailyAmountSum> responseList = response.getMbtiDailyAmountSums();
            List<MBTIDailyAmountSumResponse.MBTIDailyAmountSum> responseListWithZeroFiltering =
                    filterNonZeroAndNonEmptySums(responseList);

            assertThat(responseListWithZeroFiltering)
                    .hasSize(2);

            List<MBTIFactor> mbtiFactors = returned.stream()
                    .map(MBTIDailyAmountSumDto::getMbtiFactor)
                    .distinct()
                    .toList();
            assertThat(responseListWithZeroFiltering)
                    .extracting(MBTIDailyAmountSumResponse
                            .MBTIDailyAmountSum::getMbtiFactor)
                    .containsExactlyInAnyOrderElementsOf(mbtiFactors);

            for(var r: responseListWithZeroFiltering){
                assertDailyAmountSumGroupedByMBTI(r, returned);
            }
        }

        @Test
        void 데이터를_조회한_뒤_zero_padding을_수행한다() throws Exception {
            // given
            List<MBTIDailyAmountSumDto> returned = List.of(
                    new MBTIDailyAmountSumDto(MBTIFactor.I, LocalDate.now().minusDays(2L), 1L),
                    new MBTIDailyAmountSumDto(MBTIFactor.I, LocalDate.now().minusDays(1L), 2L),
                    new MBTIDailyAmountSumDto(MBTIFactor.E, LocalDate.now().minusDays(2L), 3L),
                    new MBTIDailyAmountSumDto(MBTIFactor.E, LocalDate.now().minusDays(1L), 4L)
            );
            when(mbtiStatisticsRepository.getAmountSumsEachMBTIAndDayBetweenStartDateAndEndDate(any(), any(), any()))
                    .thenReturn(returned);
            when(authenticatedUserService.getUserMBTI())
                    .thenReturn(Mbti.ISTJ);

            // when
            MBTIDailyAmountSumResponse response =
                    statisticsService.getAmountSumsEachMBTIAndDayLast30Days(null);

            // then
            assertThat(response.getMbti())
                    .isEqualTo(Mbti.ISTJ);

            List<MBTIDailyAmountSumResponse.MBTIDailyAmountSum> responseList = response.getMbtiDailyAmountSums();

            assertThat(responseList)
                    .extracting(MBTIDailyAmountSumResponse.MBTIDailyAmountSum::getMbtiFactor)
                    .containsExactlyInAnyOrder(MBTIFactor.values());

            List<LocalDate> localDateRanges = IntStream.rangeClosed(1, 30)
                    .mapToObj(i -> LocalDate.now().minusDays(i))
                    .toList();

            for(MBTIDailyAmountSumResponse.MBTIDailyAmountSum r: responseList){
                List<LocalDate> dates = r.getDailyAmountSums().stream()
                        .map(MBTIDailyAmountSumResponse.DailyAmountSum::getDate)
                        .toList();
                assertThat(dates)
                        .containsExactlyInAnyOrderElementsOf(localDateRanges);
            }
        }

        private static List<MBTIDailyAmountSumResponse.MBTIDailyAmountSum> filterNonZeroAndNonEmptySums(
                List<MBTIDailyAmountSumResponse.MBTIDailyAmountSum> responses) {
            return responses.stream()
                    .map(r -> {
                        List<MBTIDailyAmountSumResponse.DailyAmountSum> list =
                                r.getDailyAmountSums().stream()
                                        .filter(ea -> ea.getAmountSum() != 0)
                                        .toList();
                        return MBTIDailyAmountSumResponse
                                .MBTIDailyAmountSum.builder()
                                .mbtiFactor(r.getMbtiFactor())
                                .dailyAmountSums(list)
                                .build();
                    })
                    .filter(r -> !r.getDailyAmountSums().isEmpty())
                    .toList();
        }

        private static void assertDailyAmountSumGroupedByMBTI(
                MBTIDailyAmountSumResponse.MBTIDailyAmountSum response,
                List<MBTIDailyAmountSumDto> returned) {
            List<Tuple> dailyAmountSumGroupedByMBTI = returned.stream()
                    .filter(a -> a.getMbtiFactor()
                            .equals(response.getMbtiFactor()))
                    .map(a -> new Tuple(a.getLocalDate(), a.getAmountSum()))
                    .toList();
            assertThat(response.getDailyAmountSums())
                    .extracting("date", "amountSum")
                    .containsExactlyInAnyOrderElementsOf(dailyAmountSumGroupedByMBTI);
        }
    }

    @Nested
    class getWordFrequenciesLast30Days {
        @Test
        void UserInfoService로부터_유효하지_않은_MBTI를_받았다면_전체_유저들에_대한_메모만_요청한다() throws Exception {
            // given
            when(authenticatedUserService.getUserMBTI())
                    .thenReturn(Mbti.NONE);

            // when
            statisticsService.getWordFrequenciesLast30Days(null);

            // then
            verify(mbtiStatisticsRepository)
                    .getAllMemosByMBTIBetweenStartDateAndEndDate(any(), eq(Mbti.NONE.toString()), any(), any());
            verifyNoMoreInteractions(mbtiStatisticsRepository);
        }

        @Test
        @DisplayName("UserInfoService로부터 로그인 한 유저의 MBTI를 받아서, 전체 유저들의 메모와, 로그인 한 유저의 MBTI에 해당하는 유저들의 메모, 데이터를 총 2번 요청한다")
        void requestDataToRepositoryTest() throws Exception {
            // given
            when(authenticatedUserService.getUserMBTI())
                    .thenReturn(Mbti.ISTJ);

            // when
            MBTIWordFrequencyResponse response = statisticsService.getWordFrequenciesLast30Days(null);

            // then
            verify(mbtiStatisticsRepository, times(1))
                    .getAllMemosByMBTIBetweenStartDateAndEndDate(
                            any(),
                            eq(Mbti.NONE.toString()),
                            any(),
                            any());
            verify(mbtiStatisticsRepository, times(1))
                    .getAllMemosByMBTIBetweenStartDateAndEndDate(
                            any(),
                            eq(Mbti.ISTJ.toString()),
                            any(),
                            any());
        }

        @Test
        void 모든_메모_데이터를_조회한_뒤_이를_평면화하여_WordExtractionService에게_보낸다() throws Exception {
            // given
            List<MemoDto> memos = List.of(
                    new MemoDto("c1", "e1", "t1", "r1", "i1"),
                    new MemoDto("c2", "e2", "t2", "r2", "i2"),
                    new MemoDto("c3", "e3", "t3", "r3", "i3"),
                    new MemoDto("c4", "e4", "t4", "r4", "i4")
            );

            when(mbtiStatisticsRepository.getAllMemosByMBTIBetweenStartDateAndEndDate(any(), any(), any(), any()))
                    .thenReturn(memos);
            when(authenticatedUserService.getUserMBTI())
                    .thenReturn(Mbti.NONE);

            // when
            statisticsService.getWordFrequenciesLast30Days(null);

            // then
            List<String> flattedMemos = memos.stream()
                    .flatMap(m ->
                            Stream.of(
                                    m.getContent(),
                                    m.getThought(),
                                    m.getEvent(),
                                    m.getReason(),
                                    m.getImprovements()))
                    .toList();

            verify(wordExtractionService)
                    .analyzeWords(argThat(argument -> {
                        assertThat(argument)
                                .containsExactlyInAnyOrderElementsOf(flattedMemos);
                        return true;
                    }));
        }

        @Test
        void WordExtractionService로부터_받은_데이터와_로그인_한_유저의_MBTI를_반환한다() throws Exception {
            // given
            when(authenticatedUserService.getUserMBTI())
                    .thenReturn(Mbti.ISTJ);
            List<WordFrequency> returnedByWordExtractionService = List.of(
                    WordFrequency.builder()
                            .word("exampleWord")
                            .frequency(10L)
                            .build());
            when(wordExtractionService.analyzeWords(any()))
                    .thenReturn(returnedByWordExtractionService)
                    .thenReturn(returnedByWordExtractionService);

            // when
            MBTIWordFrequencyResponse response = statisticsService.getWordFrequenciesLast30Days(null);

            // then
            assertThat(response)
                    .extracting(MBTIWordFrequencyResponse::getMbti)
                    .isEqualTo(Mbti.ISTJ);
            assertThat(response)
                    .extracting(MBTIWordFrequencyResponse::getAllWordFrequencies)
                    .isEqualTo(returnedByWordExtractionService);
            assertThat(response)
                    .extracting(MBTIWordFrequencyResponse::getUserWordFrequencies)
                    .isEqualTo(returnedByWordExtractionService);
        }
    }

    @Nested
    class getSatisfactionAveragesEachMBTILast30Days {
        @Test
        void MBTI별_만족도_평균_데이터를_조회한_뒤_로그인_한_유저의_MBTI와_함께_반환한다() throws Exception {
            // given
            List<MBTISatisfactionAverageDto> returned = List.of(
                    MBTISatisfactionAverageDto.builder()
                            .mbtiFactor(MBTIFactor.I)
                            .satisfactionAverage(1.0f)
                            .build()
            );
            when(mbtiStatisticsRepository
                    .getSatisfactionAveragesEachMBTIBetweenStartDateAndEndDate(
                            any(),any(),any()))
                    .thenReturn(returned);
            when(authenticatedUserService.getUserMBTI())
                    .thenReturn(Mbti.ISTJ);


            // when
            MBTISatisfactionAverageResponse response = statisticsService
                    .getSatisfactionAveragesEachMBTILast30Days(null);

            // then
            assertThat(response.getMbti()).isEqualTo(Mbti.ISTJ);
            assertThat(response.getMbtiSatisfactionAverages()).isEqualTo(returned);
        }

    }
}