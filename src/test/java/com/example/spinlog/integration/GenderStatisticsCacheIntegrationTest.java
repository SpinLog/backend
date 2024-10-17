package com.example.spinlog.integration;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.article.service.ArticleService;
import com.example.spinlog.article.service.request.ArticleCreateRequest;
import com.example.spinlog.article.service.request.ArticleUpdateRequest;
import com.example.spinlog.article.service.response.WriteArticleResponseDto;
import com.example.spinlog.statistics.dto.repository.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.dto.repository.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.dto.repository.GenderSatisfactionAverageDto;
import com.example.spinlog.integration.init.GenderStatisticsCacheSetupService;
import com.example.spinlog.statistics.service.cache.GenderStatisticsCacheFallbackService;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import com.example.spinlog.util.CacheConfiguration;
import com.example.spinlog.integration.init.RepositoryDataSetupService;
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
    GenderStatisticsCacheFallbackService genderStatisticsService;
    @Autowired
    GenderStatisticsCacheSetupService genderStatisticsCacheSetupService;
    @Autowired
    RepositoryDataSetupService repositoryDataSetupService;

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
        repositoryDataSetupService.setUp();
        genderStatisticsCacheSetupService.initGenderStatisticsCache();
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
        List<GenderEmotionAmountAverageDto> geaa = genderStatisticsService
                .getAmountAveragesEachGenderAndEmotion(RegisterType.SPEND);
        List<GenderDailyAmountSumDto> gdas = genderStatisticsService
                .getAmountSumsEachGenderAndDay(RegisterType.SPEND);
        List<GenderSatisfactionAverageDto> gsa = genderStatisticsService
                .getSatisfactionAveragesEachGender(RegisterType.SPEND);

        // then
        for(var v: geaa) {
            if(v.getEmotion().equals(emotion)) {
                if(v.getGender().equals(Gender.MALE))
                    assertThat(v.getAmountAverage()).isEqualTo(maleAmountSum / maleArticleCount);
                else assertThat(v.getAmountAverage()).isEqualTo(femaleAmountSum / femaleArticleCount);
            }
            else
                assertThat(v.getAmountAverage()).isEqualTo(0);
        }
        for(var v: gdas){
            if(v.getLocalDate().equals(spendDate)) {
                if(v.getGender().equals(Gender.MALE))
                    assertThat(v.getAmountSum()).isEqualTo(maleAmountSum);
                else
                    assertThat(v.getAmountSum()).isEqualTo(femaleAmountSum);
            }
            else
                assertThat(v.getAmountSum()).isEqualTo(0);
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
        List<GenderEmotionAmountAverageDto> geaa = genderStatisticsService
                .getAmountAveragesEachGenderAndEmotion(RegisterType.SPEND);
        List<GenderDailyAmountSumDto> gdas = genderStatisticsService
                .getAmountSumsEachGenderAndDay(RegisterType.SPEND);
        List<GenderSatisfactionAverageDto> gsa = genderStatisticsService
                .getSatisfactionAveragesEachGender(RegisterType.SPEND);

        for(var v: geaa) {
            if(v.getEmotion().equals(emotion)) {
                if(v.getGender().equals(Gender.MALE))
                    assertThat(v.getAmountAverage()).isEqualTo((maleAmountSum + request.getAmount()) / (maleArticleCount + 1));
                else assertThat(v.getAmountAverage()).isEqualTo(femaleAmountSum / femaleArticleCount);
            }
            else
                assertThat(v.getAmountAverage()).isEqualTo(0);
        }
        for(var v: gdas){
            if(v.getLocalDate().equals(spendDate)) {
                if(v.getGender().equals(Gender.MALE))
                    assertThat(v.getAmountSum()).isEqualTo(maleAmountSum + request.getAmount());
                else
                    assertThat(v.getAmountSum()).isEqualTo(femaleAmountSum);
            }
            else
                assertThat(v.getAmountSum()).isEqualTo(0);
        }
        for(var v: gsa){
            if(v.getGender().equals(Gender.MALE))
                assertThat(v.getSatisfactionAverage()).isCloseTo(
                        (maleSatisfactionSum + request.getSatisfaction()) / (maleArticleCount + 1),
                        Assertions.within(0.01f));
            else
                assertThat(v.getSatisfactionAverage()).isEqualTo(femaleSatisfactionSum / femaleArticleCount);
        }
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
        List<GenderEmotionAmountAverageDto> geaa = genderStatisticsService
                .getAmountAveragesEachGenderAndEmotion(RegisterType.SPEND);
        List<GenderDailyAmountSumDto> gdas = genderStatisticsService
                .getAmountSumsEachGenderAndDay(RegisterType.SPEND);
        List<GenderSatisfactionAverageDto> gsa = genderStatisticsService
                .getSatisfactionAveragesEachGender(RegisterType.SPEND);

        for(var v: geaa) {
            if(v.getEmotion().equals(emotion)) {
                if(v.getGender().equals(Gender.MALE))
                    assertThat(v.getAmountAverage()).isEqualTo((maleAmountSum - target.getAmount()) / (maleArticleCount - 1));
                else assertThat(v.getAmountAverage()).isEqualTo(femaleAmountSum / femaleArticleCount);
            }
            else
                assertThat(v.getAmountAverage()).isEqualTo(0);
        }
        for(var v: gdas){
            if(v.getLocalDate().equals(spendDate)) {
                if(v.getGender().equals(Gender.MALE))
                    assertThat(v.getAmountSum()).isEqualTo(maleAmountSum - target.getAmount());
                else
                    assertThat(v.getAmountSum()).isEqualTo(femaleAmountSum);
            }
            else
                assertThat(v.getAmountSum()).isEqualTo(0);
        }
        for(var v: gsa){
            if(v.getGender().equals(Gender.MALE))
                assertThat(v.getSatisfactionAverage()).isCloseTo(
                        (maleSatisfactionSum - target.getSatisfaction()) / (maleArticleCount - 1),
                        Assertions.within(0.01f));
            else
                assertThat(v.getSatisfactionAverage()).isEqualTo(femaleSatisfactionSum / femaleArticleCount);
        }
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
        List<GenderEmotionAmountAverageDto> geaa = genderStatisticsService
                .getAmountAveragesEachGenderAndEmotion(RegisterType.SPEND);
        List<GenderDailyAmountSumDto> gdas = genderStatisticsService
                .getAmountSumsEachGenderAndDay(RegisterType.SPEND);
        List<GenderSatisfactionAverageDto> gsa = genderStatisticsService
                .getSatisfactionAveragesEachGender(RegisterType.SPEND);

        for(var v: geaa) {
            if(v.getEmotion().equals(emotion)) {
                if(v.getGender().equals(Gender.MALE))
                    assertThat(v.getAmountAverage()).isEqualTo((maleAmountSum - 1000 + 10000) / maleArticleCount);
                else assertThat(v.getAmountAverage()).isEqualTo(femaleAmountSum / femaleArticleCount);
            }
            else
                assertThat(v.getAmountAverage()).isEqualTo(0);
        }
        for(var v: gdas){
            if(v.getLocalDate().equals(spendDate)) {
                if(v.getGender().equals(Gender.MALE))
                    assertThat(v.getAmountSum()).isEqualTo(maleAmountSum - 1000 + 10000);
                else
                    assertThat(v.getAmountSum()).isEqualTo(femaleAmountSum);
            }
            else
                assertThat(v.getAmountSum()).isEqualTo(0);
        }
        for(var v: gsa){
            if(v.getGender().equals(Gender.MALE))
                assertThat(v.getSatisfactionAverage()).isCloseTo(
                        (maleSatisfactionSum - 1.0f + 5.0f) / maleArticleCount,
                        Assertions.within(0.01f));
            else
                assertThat(v.getSatisfactionAverage()).isEqualTo(femaleSatisfactionSum / femaleArticleCount);
        }
    }
}
