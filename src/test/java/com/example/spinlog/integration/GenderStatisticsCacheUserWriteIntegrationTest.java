package com.example.spinlog.integration;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.article.entity.RegisterType;
import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.global.security.customFilter.OAuth2ResponseImpl;
import com.example.spinlog.global.security.oauth2.user.CustomOAuth2User;
import com.example.spinlog.integration.init.RepositoryDataSetupService;
import com.example.spinlog.statistics.repository.dto.GenderDailyAmountSumDto;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.statistics.repository.dto.GenderSatisfactionAverageDto;
import com.example.spinlog.integration.init.GenderStatisticsCacheSetupService;
import com.example.spinlog.statistics.service.cache.GenderStatisticsCacheFallbackService;
import com.example.spinlog.user.dto.request.UpdateUserRequestDto;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import com.example.spinlog.user.service.UserService;
import com.example.spinlog.util.CacheConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Import({CacheConfiguration.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class GenderStatisticsCacheUserWriteIntegrationTest {
    @Autowired
    ArticleRepository articleRepository;
    @Autowired
    UserService userService;
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
        List<GenderSatisfactionAverageDto> gsa = genderStatisticsService.getSatisfactionAveragesEachGender(RegisterType.SPEND);

        // then
        for(var v: geaa) {
            if(v.getEmotion().equals(emotion)) {
                if(v.getGender().equals(Gender.MALE))
                    assertThat(v.getAmountAverage()).isEqualTo(maleAmountSum / maleArticleCount);
                else
                    assertThat(v.getAmountAverage()).isEqualTo(femaleAmountSum / femaleArticleCount);
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
    void 유저의_성별을_업데이트하면_캐시가_업데이트된다() throws Exception {
        // given
        User male = userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getGender().equals(Gender.MALE))
                .findFirst()
                .orElseThrow();
        setSecurityContext(male);

        // when
        userService.updateUserInfo(
                UpdateUserRequestDto.builder()
                        .gender(Gender.FEMALE.name())
                        .mbti(male.getMbti().name())
                        .budget(1)
                        .build());
        
        // then
        System.out.println("성별 변경 후");
        List<GenderEmotionAmountAverageDto> geaa = genderStatisticsService
                .getAmountAveragesEachGenderAndEmotion(RegisterType.SPEND);
        List<GenderDailyAmountSumDto> gdas = genderStatisticsService
                .getAmountSumsEachGenderAndDay(RegisterType.SPEND);
        List<GenderSatisfactionAverageDto> gsa = genderStatisticsService.getSatisfactionAveragesEachGender(RegisterType.SPEND);

        for(var v: geaa) {
            if(v.getGender().equals(Gender.FEMALE) && v.getEmotion().equals(emotion))
                assertThat(v.getAmountAverage()).isEqualTo(
                        (femaleAmountSum + maleAmountSum) / (femaleArticleCount + maleArticleCount));
            else
                assertThat(v.getAmountAverage()).isEqualTo(0);
        }
        for(var v: gdas){
            if(v.getGender().equals(Gender.FEMALE) && v.getLocalDate().equals(spendDate))
                assertThat(v.getAmountSum()).isEqualTo(femaleAmountSum + maleAmountSum);
            else
                assertThat(v.getAmountSum()).isEqualTo(0);

        }
        for(var v: gsa){
            if(v.getGender().equals(Gender.MALE))
                assertThat(v.getSatisfactionAverage()).isEqualTo(0);
            else
                assertThat(v.getSatisfactionAverage()).isEqualTo(
                        (femaleSatisfactionSum + maleSatisfactionSum) / (femaleArticleCount + maleArticleCount));
        }
    }

    private static void setSecurityContext(User male) {
        String[] s = male.getAuthenticationName().split("_");

        CustomOAuth2User user = CustomOAuth2User.builder()
                .oAuth2Response(
                        OAuth2ResponseImpl.builder()
                                .provider(s[0])
                                .providerId(s[1])
                                .email(male.getEmail())
                                .build())
                .firstLogin(false)
                .build();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                user.getAuthorities());
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
