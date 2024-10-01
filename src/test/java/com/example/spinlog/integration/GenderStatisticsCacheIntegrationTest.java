package com.example.spinlog.integration;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.article.service.ArticleService;
import com.example.spinlog.article.service.request.ArticleCreateRequest;
import com.example.spinlog.article.service.request.ArticleUpdateRequest;
import com.example.spinlog.article.service.response.WriteArticleResponseDto;
import com.example.spinlog.statistics.repository.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.integration.init.GenderStatisticsCacheStartupService;
import com.example.spinlog.statistics.service.GenderStatisticsService;
import com.example.spinlog.statistics.service.dto.GenderDailyAmountSumResponse;
import com.example.spinlog.statistics.service.dto.GenderEmotionAmountAverageResponse;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import com.example.spinlog.util.CacheConfiguration;
import com.example.spinlog.integration.init.DataSetupService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import({CacheConfiguration.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class GenderStatisticsCacheIntegrationTest {
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    ArticleService articleService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    GenderStatisticsService genderStatisticsService;
    @Autowired
    GenderStatisticsCacheStartupService genderStatisticsCacheStartupService;
    @Autowired
    DataSetupService dataSetupService;

    LocalDate spendDate = LocalDate.now().minusDays(1);
    Emotion emotion = Emotion.SAD;
    RegisterType registerType = RegisterType.SPEND;
    int maleArticleCount = 2;
    int femaleArticleCount = 2;
    int maleAmountSum = 1000 + 2000;
    int femaleAmountSum = 3000 + 4000;
    float maleSatisfactionSum = 1.0f + 2.0f;
    float femaleSatisfactionSum = 3.0f + 4.0f;

    @BeforeEach
    public void setUp() {
        dataSetupService.setUp();
        genderStatisticsCacheStartupService.initGenderStatisticsCache();
    }

    @AfterEach
    public void cleanup() {
        articleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    void 현재_DB의_데이터에_해당하는_캐시를_조회한다() throws Exception {
        // when
        List<GenderEmotionAmountAverageResponse> geaa = genderStatisticsService
                .getAmountAveragesEachGenderAndEmotionLast30Days(RegisterType.SPEND);
        List<GenderDailyAmountSumResponse> gdas = genderStatisticsService
                .getAmountSumsEachGenderAndDayLast30Days(RegisterType.SPEND);
        List<GenderSatisfactionAverageDto> gsa = genderStatisticsService.getSatisfactionAveragesEachGenderLast30Days(RegisterType.SPEND);

        // then
        for(var v: geaa) {
            for(var ea: v.getEmotionAmountAverages()) {
                if(!ea.getEmotion().equals(emotion)) {
                    assertThat(ea.getAmountAverage()).isEqualTo(0);
                    continue;
                }
                if(v.getGender().equals(Gender.MALE))
                    assertThat(ea.getAmountAverage()).isEqualTo(maleAmountSum / maleArticleCount);
                else
                    assertThat(ea.getAmountAverage()).isEqualTo(femaleAmountSum / femaleArticleCount);
            }
        }
        for(var v: gdas){
            for(var da: v.getDailyAmountSums()) {
                if(!da.getDate().equals(spendDate)) {
                    assertThat(da.getAmountSum()).isEqualTo(0);
                    continue;
                }

                if(v.getGender().equals(Gender.MALE))
                    assertThat(da.getAmountSum()).isEqualTo(maleAmountSum);
                else
                    assertThat(da.getAmountSum()).isEqualTo(femaleAmountSum);
            }
        }
        for(var v: gsa){
            if(v.getGender().equals(Gender.MALE))
                assertThat(v.getSatisfactionAverage()).isEqualTo(maleSatisfactionSum / maleArticleCount);
            else
                assertThat(v.getSatisfactionAverage()).isEqualTo(femaleSatisfactionSum / femaleArticleCount);
        }

        // print
        System.out.println(geaa);
        System.out.println(gdas);
        System.out.println(gsa);
    }

    @Test
    void 새로운_Article을_생성하면_캐시가_업데이트된다() throws Exception {
        // given
        User male = userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getGender().equals(Gender.MALE))
                .findFirst()
                .orElseThrow();
        ArticleCreateRequest request = ArticleCreateRequest.builder()
                .content("Test Content")
                .emotion("SAD")
                .registerType("SPEND")
                .amount(5000)
                .satisfaction(5.0f)
                .spendDate(
                        LocalDateTime.now().minusDays(1).toString())
                .build();

        // when
        WriteArticleResponseDto responseDto = articleService.createArticle(male.getAuthenticationName(), request);

        // then
        List<GenderEmotionAmountAverageResponse> geaa = genderStatisticsService
                .getAmountAveragesEachGenderAndEmotionLast30Days(RegisterType.SPEND);
        List<GenderDailyAmountSumResponse> gdas = genderStatisticsService
                .getAmountSumsEachGenderAndDayLast30Days(RegisterType.SPEND);
        List<GenderSatisfactionAverageDto> gsa = genderStatisticsService.getSatisfactionAveragesEachGenderLast30Days(RegisterType.SPEND);
        for(var v: geaa) {
            for(var ea: v.getEmotionAmountAverages()) {
                if(!ea.getEmotion().equals(emotion)) {
                    assertThat(ea.getAmountAverage()).isEqualTo(0);
                    continue;
                }
                if(v.getGender().equals(Gender.MALE))
                    assertThat(ea.getAmountAverage()).isEqualTo(
                            (maleAmountSum + request.getAmount()) / (maleArticleCount + 1));
                else
                    assertThat(ea.getAmountAverage()).isEqualTo(femaleAmountSum / femaleArticleCount);
            }
        }
        for(var v: gdas){
            for(var da: v.getDailyAmountSums()) {
                if(!da.getDate().equals(spendDate)) {
                    assertThat(da.getAmountSum()).isEqualTo(0);
                    continue;
                }

                if(v.getGender().equals(Gender.MALE))
                    assertThat(da.getAmountSum()).isEqualTo(maleAmountSum + request.getAmount());
                else
                    assertThat(da.getAmountSum()).isEqualTo(femaleAmountSum);
            }
        }
        for(var v: gsa){
            if(v.getGender().equals(Gender.MALE))
                assertThat(v.getSatisfactionAverage()).isCloseTo(
                        (maleSatisfactionSum + request.getSatisfaction()) / (maleArticleCount + 1),
                        Assertions.within(0.01f));
            else
                assertThat(v.getSatisfactionAverage()).isEqualTo(femaleSatisfactionSum / femaleArticleCount);
        }

        /*// todo 서비스가 아닌 롤백을 이용할 수 있는지 확인
        // cleanup
        articleService.deleteArticle(
                male.getAuthenticationName(),
                responseDto.getArticleId());*/
    }
    
    @Test
    void Article을_삭제하면_캐시가_업데이트된다() throws Exception {
        // given
        Article target = articleRepository.findAll().stream()
                .filter(article -> article.getAmount() == 1000)
                .findFirst()
                .orElseThrow();
        User user = userRepository.findById(target.getUser().getId())
                .orElseThrow();

        // when
        articleService.deleteArticle(
                user.getAuthenticationName(),
                target.getArticleId());

        // then
        List<GenderEmotionAmountAverageResponse> geaa = genderStatisticsService
                .getAmountAveragesEachGenderAndEmotionLast30Days(RegisterType.SPEND);
        List<GenderDailyAmountSumResponse> gdas = genderStatisticsService
                .getAmountSumsEachGenderAndDayLast30Days(RegisterType.SPEND);
        List<GenderSatisfactionAverageDto> gsa = genderStatisticsService.getSatisfactionAveragesEachGenderLast30Days(RegisterType.SPEND);
        for(var v: geaa) {
            for(var ea: v.getEmotionAmountAverages()) {
                if(!ea.getEmotion().equals(emotion)) {
                    assertThat(ea.getAmountAverage()).isEqualTo(0);
                    continue;
                }
                if(v.getGender().equals(Gender.MALE))
                    assertThat(ea.getAmountAverage()).isEqualTo(
                            (maleAmountSum - target.getAmount()) / (maleArticleCount - 1));
                else
                    assertThat(ea.getAmountAverage()).isEqualTo(femaleAmountSum / femaleArticleCount);
            }
        }
        for(var v: gdas){
            for(var da: v.getDailyAmountSums()) {
                if(!da.getDate().equals(spendDate)) {
                    assertThat(da.getAmountSum()).isEqualTo(0);
                    continue;
                }

                if(v.getGender().equals(Gender.MALE))
                    assertThat(da.getAmountSum()).isEqualTo(maleAmountSum - target.getAmount());
                else
                    assertThat(da.getAmountSum()).isEqualTo(femaleAmountSum);
            }
        }
        for(var v: gsa){
            if(v.getGender().equals(Gender.MALE))
                assertThat(v.getSatisfactionAverage()).isCloseTo(
                        (maleSatisfactionSum - target.getSatisfaction()) / (maleArticleCount - 1),
                        Assertions.within(0.01f));
            else
                assertThat(v.getSatisfactionAverage()).isEqualTo(femaleSatisfactionSum / femaleArticleCount);
        }

        /*// cleanup
        articleService.createArticle(
                user.getAuthenticationName(),
                ArticleCreateRequest.builder()
                        .content(target.getContent())
                        .amount(target.getAmount())
                        .satisfaction(target.getSatisfaction())
                        .registerType(target.getRegisterType().name())
                        .spendDate(target.getSpendDate().toString())
                        .emotion(target.getEmotion().name())
                        .build()
        );*/
    }

    @Test
    void Article을_수정하면_캐시가_업데이트된다() throws Exception {
        // given
        Article target = articleRepository.findAll().stream()
                .filter(article -> article.getAmount() == 1000)
                .findFirst()
                .orElseThrow();
        User user = userRepository.findById(target.getUser().getId())
                .orElseThrow();

        ArticleUpdateRequest updateRequest = ArticleUpdateRequest.builder()
                .content(target.getContent())
                .spendDate(target.getSpendDate().toString())
                .emotion(target.getEmotion().name())
                .satisfaction(5.0f)
                .amount(10000)
                .registerType(target.getRegisterType().name())
                .build();

        // when
        articleService.updateArticle(
                user.getAuthenticationName(),
                target.getArticleId(),
                updateRequest);

        // then
        List<GenderEmotionAmountAverageResponse> geaa = genderStatisticsService
                .getAmountAveragesEachGenderAndEmotionLast30Days(RegisterType.SPEND);
        List<GenderDailyAmountSumResponse> gdas = genderStatisticsService
                .getAmountSumsEachGenderAndDayLast30Days(RegisterType.SPEND);
        List<GenderSatisfactionAverageDto> gsa = genderStatisticsService.getSatisfactionAveragesEachGenderLast30Days(RegisterType.SPEND);
        for(var v: geaa) {
            for(var ea: v.getEmotionAmountAverages()) {
                if(!ea.getEmotion().equals(emotion)) {
                    assertThat(ea.getAmountAverage()).isEqualTo(0);
                    continue;
                }
                if(v.getGender().equals(Gender.MALE))
                    assertThat(ea.getAmountAverage()).isEqualTo(
                            (maleAmountSum - 1000 + 10000) / maleArticleCount);
                else
                    assertThat(ea.getAmountAverage()).isEqualTo(femaleAmountSum / femaleArticleCount);
            }
        }
        for(var v: gdas){
            for(var da: v.getDailyAmountSums()) {
                if(!da.getDate().equals(spendDate)) {
                    assertThat(da.getAmountSum()).isEqualTo(0);
                    continue;
                }

                if(v.getGender().equals(Gender.MALE))
                    assertThat(da.getAmountSum()).isEqualTo(maleAmountSum - 1000 + 10000);
                else
                    assertThat(da.getAmountSum()).isEqualTo(femaleAmountSum);
            }
        }
        for(var v: gsa){
            if(v.getGender().equals(Gender.MALE))
                assertThat(v.getSatisfactionAverage()).isCloseTo(
                        (maleSatisfactionSum - 1.0f + 5.0f) / maleArticleCount,
                        Assertions.within(0.01f));
            else
                assertThat(v.getSatisfactionAverage()).isEqualTo(femaleSatisfactionSum / femaleArticleCount);
        }

        /*// cleanup
        articleService.updateArticle(
                user.getAuthenticationName(),
                target.getArticleId(),
                ArticleUpdateRequest.builder()
                        .content(target.getContent())
                        .spendDate(target.getSpendDate().toString())
                        .emotion(target.getEmotion().name())
                        .satisfaction(1.0f)
                        .reason(target.getReason())
                        .amount(1000)
                        .registerType(target.getRegisterType().name())
                        .build()
        );*/
    }
}
