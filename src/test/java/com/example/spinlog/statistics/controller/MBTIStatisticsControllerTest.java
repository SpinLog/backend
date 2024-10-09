package com.example.spinlog.statistics.controller;

import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.statistics.service.MBTIStatisticsService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(MBTIStatisticsController.class)
@WithMockUser
@MockBean(JpaMetamodelMappingContext.class) // @EnableJpaAuditing 때문에 추가함 TODO (이 어노테이션을 configuration으로 분리하면 없앨 수 있음)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MBTIStatisticsControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockBean
    MBTIStatisticsService mbtiStatisticsService;

    @Nested
    class MBTI별_감정별_금액_평균_통계_API {
        @Test
        @DisplayName("/api/statistics/mbti/emotion/amounts/average 로 요청하면 getAmountAveragesEachMBTIAndEmotionLast30Days 메서드가 실행된다.")
        void 아래_path로_요청하면_statisticsService의_getAmountAverage_메서드가_실행된다() throws Exception {
            // when
            mockMvc.perform(get("/api/statistics/mbti/emotion/amounts/average"));

            // then
            verify(mbtiStatisticsService, times(1))
                    .getAmountAveragesEachMBTIAndEmotionLast30Days(any());
        }

        @Test
        void registerType_쿼리_파라미터를_입력하지_않으면_defaultValue로_SPEND가_입력된다() throws Exception {
            // when
            mockMvc.perform(get("/api/statistics/mbti/emotion/amounts/average"));

            // then
            verify(mbtiStatisticsService)
                    .getAmountAveragesEachMBTIAndEmotionLast30Days(eq(RegisterType.SPEND));
        }

        @Test
        void 올바르지_않은_registerType_쿼리_파라미터를_입력하면_400을_반환한다() throws Exception {
            // given
            String invalidRegisterType = "Invalid";

            // when // then
            mockMvc.perform(
                    get("/api/statistics/mbti/emotion/amounts/average?registerType=" + invalidRegisterType))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"SPEND", "SAVE"})
        void registerType_쿼리_파라미터로_SPEND나_SAVE를_입력해야_한다(String registerType) throws Exception {
            // when
            mockMvc.perform(get("/api/statistics/mbti/emotion/amounts/average?registerType=" + registerType));

            // then
            verify(mbtiStatisticsService, times(1))
                    .getAmountAveragesEachMBTIAndEmotionLast30Days(eq(RegisterType.valueOf(registerType)));
        }
    }

    @Nested
    class MBTI별_일별_금액_총합_통계_API {
        @Test
        @DisplayName("/api/statistics/mbti/daily/amounts/sum 로 요청하면 getAmountSumsEachMBTIAndDayLast30Days 메서드가 실행된다.")
        void 아래_path로_요청하면_statisticsService의_getAmountSums_메서드가_실행된다() throws Exception {
            // when
            mockMvc.perform(get("/api/statistics/mbti/daily/amounts/sum"));

            // then
            verify(mbtiStatisticsService, times(1))
                    .getAmountSumsEachMBTIAndDayLast30Days(any());
        }

        @Test
        void registerType_쿼리_파라미터를_입력하지_않으면_defaultValue로_SPEND가_입력된다() throws Exception {
            // when
            mockMvc.perform(get("/api/statistics/mbti/daily/amounts/sum"));

            // then
            verify(mbtiStatisticsService)
                    .getAmountSumsEachMBTIAndDayLast30Days(eq(RegisterType.SPEND));
        }

        @Test
        void 올바르지_않은_registerType_쿼리_파라미터를_입력하면_400을_반환한다() throws Exception {
            // given
            String invalidRegisterType = "Invalid";

            // when // then
            mockMvc.perform(
                    get("/api/statistics/mbti/daily/amounts/sum?registerType=" + invalidRegisterType))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"SPEND", "SAVE"})
        void registerType_쿼리_파라미터로_SPEND나_SAVE를_입력해야_한다(String registerType) throws Exception {
            // when
            mockMvc.perform(get("/api/statistics/mbti/daily/amounts/sum?registerType=" + registerType));

            // then
            verify(mbtiStatisticsService, times(1))
                    .getAmountSumsEachMBTIAndDayLast30Days(eq(RegisterType.valueOf(registerType)));
        }
    }

    @Nested
    class MBTI별_단어_빈도수_통계_API {
        @Test
        @DisplayName("/api/statistics/mbti/word/frequencies 로 요청하면 getWordFrequenciesLast30Days 메서드가 실행된다.")
        void 아래_path로_요청하면_statisticsService의_getWordFrequencies_메서드가_실행된다() throws Exception {
            // when
            mockMvc.perform(get("/api/statistics/mbti/word/frequencies"));

            // then
            verify(mbtiStatisticsService, times(1))
                    .getWordFrequenciesLast30Days(any());
        }

        @Test
        void registerType_쿼리_파라미터를_입력하지_않으면_defaultValue로_SPEND가_입력된다() throws Exception {
            // when
            mockMvc.perform(get("/api/statistics/mbti/word/frequencies"));

            // then
            verify(mbtiStatisticsService)
                    .getWordFrequenciesLast30Days(eq(RegisterType.SPEND));
        }

        @Test
        void 올바르지_않은_registerType_쿼리_파라미터를_입력하면_400을_반환한다() throws Exception {
            // given
            String invalidRegisterType = "Invalid";

            // when // then
            mockMvc.perform(
                            get("/api/statistics/mbti/word/frequencies?registerType=" + invalidRegisterType))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"SPEND", "SAVE"})
        void registerType_쿼리_파라미터로_SPEND나_SAVE를_입력해야_한다(String registerType) throws Exception {
            // when
            mockMvc.perform(get("/api/statistics/mbti/word/frequencies?registerType=" + registerType));

            // then
            verify(mbtiStatisticsService, times(1))
                    .getWordFrequenciesLast30Days(eq(RegisterType.valueOf(registerType)));
        }
    }

    @Nested
    class MBTI별_만족도_평균_통계_API {
        @Test
        @DisplayName("/api/statistics/mbti/satisfactions/average 로 요청하면 getSatisfactionAveragesEachMBTILast30Days 메서드가 실행된다.")
        void 아래_path로_요청하면_statisticsService의_getSatisfactionAverages_메서드가_실행된다() throws Exception {
            // when
            mockMvc.perform(get("/api/statistics/mbti/satisfactions/average"));

            // then
            verify(mbtiStatisticsService, times(1))
                    .getSatisfactionAveragesEachMBTILast30Days(any());
        }

        @Test
        void registerType_쿼리_파라미터를_입력하지_않으면_defaultValue로_SPEND가_입력된다() throws Exception {
            // when
            mockMvc.perform(get("/api/statistics/mbti/satisfactions/average"));

            // then
            verify(mbtiStatisticsService)
                    .getSatisfactionAveragesEachMBTILast30Days(eq(RegisterType.SPEND));
        }

        @Test
        void 올바르지_않은_registerType_쿼리_파라미터를_입력하면_400을_반환한다() throws Exception {
            // given
            String invalidRegisterType = "Invalid";

            // when // then
            mockMvc.perform(get("/api/statistics/mbti/satisfactions/average?registerType=" + invalidRegisterType))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @ValueSource(strings = {"SPEND", "SAVE"})
        void registerType_쿼리_파라미터로_SPEND나_SAVE를_입력해야_한다(String registerType) throws Exception {
            // when
            mockMvc.perform(get("/api/statistics/mbti/satisfactions/average?registerType=" + registerType));

            // then
            verify(mbtiStatisticsService, times(1))
                    .getSatisfactionAveragesEachMBTILast30Days(eq(RegisterType.valueOf(registerType)));
        }
    }
}