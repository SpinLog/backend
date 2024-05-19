package com.example.spinlog.ai.service;

import com.example.spinlog.ai.dto.*;
import com.example.spinlog.article.dto.WriteArticleRequestDto;
import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.article.service.ArticleService;
import com.example.spinlog.global.security.oauth2.user.CustomOAuth2User;
import com.example.spinlog.user.custom.securitycontext.WithMockCustomOAuth2User;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.Mbti;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.spinlog.user.custom.securitycontext.OAuth2Provider.KAKAO;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WireMockConfig.class})
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
@Slf4j
public class AiServiceTest {

    @Value("${apiKey}")
    private String testApiKey;

    @Autowired
    private OpenAiClient openAiClient;

    @Mock
    private ArticleService articleService;

    @InjectMocks
    private AiServiceImpl aiService;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired  // userId가 필요해 Autowired 설정
    private UserRepository userRepository;

    @Autowired  // articleId가 필요해 Autowired 설정
    private ArticleRepository articleRepository;

    private final static String AI_MODEL = "gpt-3.5-turbo";
    private final static String AI_ROLE = "system";
    private final static String USER_ROLE = "user";
    private final static String MESSAGE_TO_AI = "Your identity is as an advice giver.\n" +
            "This data shows that people in their 20s and 30s made consumption due to emotional expression. Could you give me some advice on the connection between emotions and consumption?\n" +
            "===Please answer by referring to the rules below==\n" +
            "First of all, empathize with the user. At this time, mention emotions, events, and purchase details.\n" +
            "Please tell us 3 areas for improvement along with reasons.\n" +
            "Please use a total of 50 Korean words.\n" +
            "Speak in a friendly manner, as if you were talking to a friend.";

    @BeforeEach
    void setUp() {
        AiMocks.setupAiMockResponse(wireMockServer);
    }

    @Test
    @WithMockCustomOAuth2User(
            provider = KAKAO, email = "kakaoemail@kakao.com", providerMemberId = "123ab", isFirstLogin = false
    )
    void api_요청_성공_테스트() throws IOException {
        // Given
        CustomOAuth2User oAuth2User = (CustomOAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String authenticationName = oAuth2User.getOAuth2Response().getAuthenticationName();
        User buildUser = User.builder()
                .email(oAuth2User.getName())
                .mbti(Mbti.ISTP)
                .gender(Gender.MALE)
                .budget(12345)
                .authenticationName(authenticationName)
                .build();
        User user = userRepository.save(buildUser);

        WriteArticleRequestDto WriteRequestDto = WriteArticleRequestDto.builder()
                .content("투썸플레이스 아이스아메리카노")
                .spendDate("2024-04-04T11:22:33")
                .event("부장님께 혼남")
                .thought("회사 그만 두고 싶다")
                .emotion("ANNOYED")
                .satisfaction(2F)
                .reason("홧김에 돈 쓴게 마음에 들지 않는다")
                .improvements("소비 전에 한번 더 생각하고 참아본다")
                .amount(5000)
                .registerType("SPEND")
                .build();
        Article article = articleRepository.save(WriteRequestDto.toEntity(user));

        AiRequestDto requestDto = AiRequestDto.builder()
                .articleId(article.getArticleId())
                .build();

        List<Message> messages = new ArrayList<>();
        Message message1 = Message.builder()
                .role(AI_ROLE)
                .content(MESSAGE_TO_AI)
                .build();
        Message message2 = Message.builder()
                .role(USER_ROLE)
                .content(requestDto.toString())
                .build();
        messages.add(message1);
        messages.add(message2);

        CommentRequest commentRequest = CommentRequest.builder()
                .model(AI_MODEL)
                .messages(messages)
                .build();

        CommentResponse commentResponse = openAiClient.getAiComment(testApiKey, commentRequest);

        assertThat(commentResponse).isNotNull();
    }
}
