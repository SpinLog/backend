package com.example.spinlog.statistics.service;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.repository.GenderStatisticsRepository;
import com.example.spinlog.statistics.repository.dto.*;
import com.example.spinlog.statistics.service.cache.GenderStatisticsCacheFallbackService;
import com.example.spinlog.statistics.service.dto.*;
import com.example.spinlog.statistics.service.wordanalysis.WordExtractionService;
import com.example.spinlog.user.entity.Gender;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class GenderStatisticsServiceTest {
    @Mock
    WordExtractionService wordExtractionService;
    @Mock
    GenderStatisticsRepository genderStatisticsRepository;
    @Mock
    GenderStatisticsCacheFallbackService genderStatisticsCacheFallbackService;

    @InjectMocks
    GenderStatisticsService statisticsService;

    @Nested
    class getAmountAveragesEachGenderAndEmotionLast30Days{
        @Test
        void 성별_감정별_금액_평균_데이터를_조회한_뒤_성별로_grouping해서_반환한다() throws Exception {
            // given
            List<GenderEmotionAmountAverageDto> returned = List.of(
                    new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.PROUD, 1000L),
                    new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 2000L),
                    new GenderEmotionAmountAverageDto(Gender.FEMALE, Emotion.PROUD, 3000L),
                    new GenderEmotionAmountAverageDto(Gender.FEMALE, Emotion.SAD, 4000L)
            );

            when(genderStatisticsCacheFallbackService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenReturn(returned);

            // when
            List<GenderEmotionAmountAverageResponse> responses =
                    statisticsService.getAmountAveragesEachGenderAndEmotionLast30Days(null);

            // then
            List<GenderEmotionAmountAverageResponse> responsesWithZeroFiltering = filterNonZeroAndNonEmptyAverages(responses);

            assertThat(responsesWithZeroFiltering)
                    .hasSize(2);

            List<Gender> genders = returned.stream()
                    .map(GenderEmotionAmountAverageDto::getGender)
                    .distinct()
                    .toList();
            assertThat(responsesWithZeroFiltering)
                    .extracting(GenderEmotionAmountAverageResponse::getGender)
                    .containsExactlyInAnyOrderElementsOf(genders);

            for(var response: responsesWithZeroFiltering){
                assertEmotionAmountAveragesGroupedByGender(response, returned);
            }
        }

        @Test
        void 조회한_데이터에_zero_padding을_수행한다() throws Exception {
            // given
            List<GenderEmotionAmountAverageDto> returned = List.of(
                    new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.PROUD, 1L),
                    new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 2L)
            );

            when(genderStatisticsCacheFallbackService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenReturn(returned);

            // when
            List<GenderEmotionAmountAverageResponse> responses =
                    statisticsService.getAmountAveragesEachGenderAndEmotionLast30Days(null);

            // then
            assertThat(responses)
                    .extracting(GenderEmotionAmountAverageResponse::getGender)
                    .containsExactlyInAnyOrderElementsOf(List.of(Gender.MALE, Gender.FEMALE));
            assertThat(responses)
                    .extracting(GenderEmotionAmountAverageResponse::getEmotionAmountAverages)
                    .allMatch(list ->
                            list.stream()
                                    .map(GenderEmotionAmountAverageResponse.EmotionAmountAverage::getEmotion)
                                    .allMatch(Arrays.asList(Emotion.values())::contains));
        }
        
        @Test
        void 천의_자리에서_반올림하여_반환한다() throws Exception {
            // given
            List<GenderEmotionAmountAverageDto> returned = List.of(
                    new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.PROUD, 1234L),
                    new GenderEmotionAmountAverageDto(Gender.MALE, Emotion.SAD, 2468L)
            );

            when(genderStatisticsCacheFallbackService.getAmountAveragesEachGenderAndEmotion(any()))
                    .thenReturn(returned);

            // when
            List<GenderEmotionAmountAverageResponse> responses =
                    statisticsService.getAmountAveragesEachGenderAndEmotionLast30Days(null);

            // then
            assertThat(responses)
                    .extracting(GenderEmotionAmountAverageResponse::getEmotionAmountAverages)
                    .allMatch(list ->
                            list.stream()
                                    .map(GenderEmotionAmountAverageResponse.EmotionAmountAverage::getAmountAverage)
                                    .allMatch(a -> a % 1000 == 0));
        }

        private static List<GenderEmotionAmountAverageResponse> filterNonZeroAndNonEmptyAverages(List<GenderEmotionAmountAverageResponse> responses) {
            return responses.stream()
                    .map(r -> {
                        List<GenderEmotionAmountAverageResponse.EmotionAmountAverage> list =
                                r.getEmotionAmountAverages().stream()
                                        .filter(ea -> ea.getAmountAverage() != 0)
                                        .toList();
                        return GenderEmotionAmountAverageResponse.builder()
                                .gender(r.getGender())
                                .emotionAmountAverages(list)
                                .build();
                    })
                    .filter(r -> !r.getEmotionAmountAverages().isEmpty())
                    .toList();
        }

        private static void assertEmotionAmountAveragesGroupedByGender(
                GenderEmotionAmountAverageResponse response,
                List<GenderEmotionAmountAverageDto> returned
        ) {
            List<Tuple> emotionAmountAveragesGroupedByGender = returned.stream()
                    .filter(a -> a.getGender()
                            .equals(response.getGender()))
                    .map(a -> new Tuple(a.getEmotion(),
                            a.getAmountAverage()))
                    .toList();
            assertThat(response.getEmotionAmountAverages())
                    .extracting("emotion", "amountAverage")
                    .containsExactlyInAnyOrderElementsOf(emotionAmountAveragesGroupedByGender);
        }
    }

    @Nested
    class getAmountSumsEachGenderAndDayLast30Days {
        @Test
        void 성별_일별_금액_총합_데이터를_조회한_뒤_성별로_grouping해서_반환한다() throws Exception {
            // given
            List<GenderDailyAmountSumDto> returned = List.of(
                    new GenderDailyAmountSumDto(Gender.MALE, LocalDate.now().minusDays(2L), 1L),
                    new GenderDailyAmountSumDto(Gender.MALE, LocalDate.now().minusDays(1L), 2L),
                    new GenderDailyAmountSumDto(Gender.FEMALE, LocalDate.now().minusDays(2L), 3L),
                    new GenderDailyAmountSumDto(Gender.FEMALE, LocalDate.now().minusDays(1L), 4L)
            );
            when(genderStatisticsCacheFallbackService.getAmountSumsEachGenderAndDay(any()))
                    .thenReturn(returned);

            // when
            List<GenderDailyAmountSumResponse> responses =
                    statisticsService.getAmountSumsEachGenderAndDayLast30Days(null);

            // then
            List<GenderDailyAmountSumResponse> responsesWithZeroFiltering = filterNonZeroAndNonEmptySums(responses);

            assertThat(responsesWithZeroFiltering)
                    .hasSize(2);

            List<Gender> genders = returned.stream()
                    .map(GenderDailyAmountSumDto::getGender)
                    .distinct()
                    .toList();
            assertThat(responsesWithZeroFiltering)
                    .extracting(GenderDailyAmountSumResponse::getGender)
                    .containsExactlyInAnyOrderElementsOf(genders);

            for(var response: responsesWithZeroFiltering){
                assertDailyAmountSumGroupedByGender(response, returned);
            }
        }

        @Test
        void 조회한_데이터에_zero_padding을_수행한다() throws Exception {
            // given
            List<GenderDailyAmountSumDto> returned = List.of(
                    new GenderDailyAmountSumDto(Gender.MALE, LocalDate.now().minusDays(2L), 1L),
                    new GenderDailyAmountSumDto(Gender.MALE, LocalDate.now().minusDays(1L), 2L)
            );
            when(genderStatisticsCacheFallbackService.getAmountSumsEachGenderAndDay(any()))
                    .thenReturn(returned);

            // when
            List<GenderDailyAmountSumResponse> responses =
                    statisticsService.getAmountSumsEachGenderAndDayLast30Days(null);

            // then
            assertThat(responses)
                    .extracting(GenderDailyAmountSumResponse::getGender)
                    .containsExactlyInAnyOrderElementsOf(List.of(Gender.MALE, Gender.FEMALE));

            List<LocalDate> localDateRanges = IntStream.rangeClosed(1, 30)
                    .mapToObj(i -> LocalDate.now().minusDays(i))
                    .toList();

            for(GenderDailyAmountSumResponse r: responses){
                assertThat(r.getDailyAmountSums())
                        .extracting("date")
                        .containsExactlyInAnyOrderElementsOf(localDateRanges);
            }
        }

        private static List<GenderDailyAmountSumResponse> filterNonZeroAndNonEmptySums(List<GenderDailyAmountSumResponse> responses) {
            return responses.stream()
                    .map(r -> {
                        List<GenderDailyAmountSumResponse.DailyAmountSum> list =
                                r.getDailyAmountSums().stream()
                                        .filter(ea -> ea.getAmountSum() != 0)
                                        .toList();
                        return GenderDailyAmountSumResponse.builder()
                                .gender(r.getGender())
                                .dailyAmountSums(list)
                                .build();
                    })
                    .filter(r -> !r.getDailyAmountSums().isEmpty())
                    .toList();
        }

        private static void assertDailyAmountSumGroupedByGender(
                GenderDailyAmountSumResponse response,
                List<GenderDailyAmountSumDto> returned
        ) {
            List<Tuple> emotionAmountSumsGroupedByGender = returned.stream()
                    .filter(a -> a.getGender()
                            .equals(response.getGender()))
                    .map(a -> new Tuple(
                            a.getLocalDate(),
                            a.getAmountSum()))
                    .toList();
            assertThat(response.getDailyAmountSums())
                    .extracting("date", "amountSum")
                    .containsExactlyInAnyOrderElementsOf(emotionAmountSumsGroupedByGender);
        }
    }

    @Nested
    class getWordFrequenciesEachGenderLast30Days {
        @Test
        void 모든_메모_데이터를_조회한_뒤_이를_평면화하여_WordExtractionService에게_보낸다() throws Exception {
            // given
            List<MemoDto> memos = List.of(
                    new MemoDto("c1", "e1", "t1", "r1", "i1"),
                    new MemoDto("c2", "e2", "t2", "r2", "i2"),
                    new MemoDto("c3", "e3", "t3", "r3", "i3"),
                    new MemoDto("c4", "e4", "t4", "r4", "i4")
            );
            when(genderStatisticsRepository.getAllMemosByGenderBetweenStartDateAndEndDate(any(), any(), any(), any()))
                    .thenReturn(memos)
                    .thenReturn(memos);

            // when
            statisticsService.getWordFrequenciesEachGenderLast30Days(null);

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
            verify(wordExtractionService, times(2))
                    .analyzeWords(argThat(argument -> {
                        assertThat(argument)
                                .containsExactlyInAnyOrderElementsOf(flattedMemos);
                        return true;
                    }));
        }
        
        @Test
        void WordExtractionService로부터_성별로_메모에_대한_단어_빈도수_데이터를_받아서_하나의_객체로_반환한다() throws Exception {
            // givenq
            List<WordFrequency> returnedByWordExtractionService = List.of(
                    WordFrequency.builder()
                            .word("exampleWord")
                            .frequency(10L)
                            .build());
            when(wordExtractionService.analyzeWords(any()))
                    .thenReturn(returnedByWordExtractionService)
                    .thenReturn(returnedByWordExtractionService);
            
            // when
            GenderWordFrequencyResponse response = statisticsService.getWordFrequenciesEachGenderLast30Days(null);

            // then
            assertThat(response)
                    .extracting(GenderWordFrequencyResponse::getMaleWordFrequencies)
                    .isEqualTo(returnedByWordExtractionService);
            assertThat(response)
                    .extracting(GenderWordFrequencyResponse::getFemaleWordFrequencies)
                    .isEqualTo(returnedByWordExtractionService);
        }
    }

    @Nested
    class getSatisfactionAveragesEachGenderLast30Days {
        @Test
        void 성별_만족도_평균_데이터를_조회한_뒤_그대로_반환한다() throws Exception {
            // given
            List<GenderSatisfactionAverageDto> returned = List.of(
                    GenderSatisfactionAverageDto.builder()
                            .gender(Gender.MALE)
                            .satisfactionAverage(1.0f)
                            .build());
            when(genderStatisticsCacheFallbackService.getSatisfactionAveragesEachGender(any()))
                    .thenReturn(returned);

            // when
            List<GenderSatisfactionAverageDto> response = statisticsService
                    .getSatisfactionAveragesEachGenderLast30Days(null);

            // then
            assertThat(response).isEqualTo(returned);
        }

        @Test
        void 소수_첫째_자리에서_반올림하여_반환한다() throws Exception {
            // given
            List<GenderSatisfactionAverageDto> returned = List.of(
                    GenderSatisfactionAverageDto.builder()
                            .gender(Gender.MALE)
                            .satisfactionAverage(1.234f)
                            .build());
            when(genderStatisticsCacheFallbackService.getSatisfactionAveragesEachGender(any()))
                    .thenReturn(returned);

            // when
            List<GenderSatisfactionAverageDto> response = statisticsService
                    .getSatisfactionAveragesEachGenderLast30Days(null);

            // then
            assertThat(response.get(0).getSatisfactionAverage())
                    .isEqualTo(1.2f);
        }
    }
}