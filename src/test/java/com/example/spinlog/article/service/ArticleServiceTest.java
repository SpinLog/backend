package com.example.spinlog.article.service;

import com.example.spinlog.article.service.request.ArticleUpdateRequest;
import com.example.spinlog.article.service.request.SearchCond;
import com.example.spinlog.article.service.response.ViewArticleResponseDto;
import com.example.spinlog.article.service.response.ViewArticleSumDto;
import com.example.spinlog.article.service.response.ViewListResponseDto;
import com.example.spinlog.article.service.response.WriteArticleResponseDto;
import com.example.spinlog.article.entity.Article;
import com.example.spinlog.article.repository.ArticleRepository;
import com.example.spinlog.article.service.request.ArticleCreateRequest;
import com.example.spinlog.global.error.exception.article.ArticleNotFoundException;
import com.example.spinlog.global.error.exception.user.UnauthorizedArticleRequestException;
import com.example.spinlog.global.error.exception.user.UserNotFoundException;
import com.example.spinlog.user.entity.User;
import com.example.spinlog.user.repository.UserRepository;
import com.example.spinlog.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.spinlog.article.entity.Emotion.ANNOYED;
import static com.example.spinlog.article.entity.RegisterType.SAVE;
import static com.example.spinlog.article.entity.RegisterType.SPEND;
import static com.example.spinlog.user.entity.Gender.MALE;
import static com.example.spinlog.user.entity.Mbti.ISTJ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ActiveProfiles("test")
@Transactional
@SpringBootTest
public class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ArticleService articleService;
    @Autowired
    private UserService userService;

    @DisplayName("일기 작성 요청을 받아 일기를 생성한다.")
    @Test
    void creatArticle() {
        // Given
        User user = createUser();
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.of(user));

        Article article = createArticle(user);
        given(articleRepository.save(any(Article.class))).willReturn(article);

        ArticleCreateRequest requestDto = createArticleCreateRequest();

        // When
        WriteArticleResponseDto response = articleService.createArticle(user.getAuthenticationName(), requestDto);

        // Then
        assertThat(response).isNotNull();

        Long savedArticleId = user.getArticles().stream()
                .findAny().get().getArticleId();
        assertThat(savedArticleId).isEqualTo(response.getArticleId());
        verify(articleRepository, times(1)).save(any(Article.class));
    }


    @DisplayName("필터없이 조회시 저장된 모든 일기를 반환한다.")
    @Test
    void listArticles() {
        // Given
        User user = createUser();
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 10);
        SearchCond cond = SearchCond.builder()
                .registerTypes(List.of(SPEND))
                .emotions(List.of(ANNOYED))
                .satisfactions(List.of(5F))
                .words(List.of(""))
                .build();
        Page<ViewArticleSumDto> articlePages = new PageImpl<>(Collections.singletonList(ViewArticleSumDto.builder().build()));
        given(articleRepository.search(any(User.class), any(Pageable.class), any(SearchCond.class))).willReturn(articlePages);

        // When
        ViewListResponseDto result = articleService.listArticles("test user", pageable, cond);

        // Then
        verify(userRepository).findByAuthenticationName("test user");
        verify(articleRepository).search(user, pageable, cond);

        assertThat(articlePages.getContent()).isEqualTo(result.getSpendList());
        assertThat(!articlePages.isLast()).isEqualTo(result.isNextPage());
    }

    @DisplayName("저장된 일기가 없으면 빈 리스트를 반환한다.")
    @Test
    void listArticles_fail() {
        // Given
        User user = createUser();
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 10);
        SearchCond cond = SearchCond.builder()
                .registerTypes(List.of(SPEND))
                .emotions(List.of(ANNOYED))
                .satisfactions(List.of(5F))
                .words(List.of(""))
                .build();
        Page<ViewArticleSumDto> articlePages = new PageImpl<>(Collections.emptyList());
        given(articleRepository.search(any(User.class), any(Pageable.class), any(SearchCond.class))).willReturn(articlePages);

        // When
        ViewListResponseDto result = articleService.listArticles("test user", pageable, cond);

        // Then
        verify(userRepository).findByAuthenticationName("test user");
        verify(articleRepository).search(user, pageable, cond);

        assertThat(result.getSpendList()).isEmpty(); // 결과가 빈 리스트인지 확인
        assertThat(result.isNextPage()).isFalse(); // 다음 페이지가 없는지 확인
    }

    @DisplayName("검색 필터가 있으면 필터에 해당하는 일기만 반환한다.")
    @Test
    void listArticles_filtered() {
        // given
        User user = createUser();
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.of(user));

        Pageable pageable = PageRequest.of(0, 10);
        SearchCond cond = SearchCond.builder()
                .registerTypes(List.of(SPEND))
                .emotions(List.of(ANNOYED))
                .satisfactions(List.of(5F))
                .words(List.of("Hello", "Hi"))
                .build();

        ViewArticleSumDto article1 = ViewArticleSumDto.builder()
                .articleId(1L)
                .content("Hello")
                .satisfaction(5F)
                .amount(1000)
                .emotion(ANNOYED)
                .registerType(SPEND)
                .build();
        ViewArticleSumDto article2 = ViewArticleSumDto.builder()
                .articleId(1L)
                .content("Hi")
                .satisfaction(5F)
                .amount(1000)
                .emotion(ANNOYED)
                .registerType(SPEND)
                .build();
        ViewArticleSumDto article3 = ViewArticleSumDto.builder()
                .articleId(1L)
                .content("Bye")
                .satisfaction(5F)
                .amount(1000)
                .emotion(ANNOYED)
                .registerType(SPEND)
                .build();

        List<ViewArticleSumDto> filteredArticles = List.of(article1, article2);
        Page<ViewArticleSumDto> articlePages = new PageImpl<>(filteredArticles, pageable, filteredArticles.size());
        given(articleRepository.search(any(User.class), any(Pageable.class), any(SearchCond.class))).willReturn(articlePages);

        // When
        ViewListResponseDto result = articleService.listArticles("test user", pageable, cond);

        // then
        verify(userRepository).findByAuthenticationName("test user");
        verify(articleRepository).search(user, pageable, cond);

        assertThat(result.getSpendList()).containsExactlyInAnyOrder(article1, article2);
        assertThat(result.getSpendList()).doesNotContain(article3);
        assertThat(result.isNextPage()).isFalse();
    }

    @DisplayName("일기 ID를 입력하면 일기를 반환한다.")
    @Test
    void getArticle() {
        // given
        User user = createUser();
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.of(user));

        Article article = createArticle(user);
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(article));

        // when
        ViewArticleResponseDto result = articleService.getArticle("test user", 1L);

        // then
        verify(userRepository).findByAuthenticationName("test user");
        verify(articleRepository).findById(1L);
        assertThat(result).isNotNull();
    }

    @DisplayName("사용자 이름으로 사용자를 찾지 못하면 UserNotFound 예외를 반환한다.")
    @Test
    void getArticle_userNotFound() {
        // given
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleService.getArticle("test user", 1L))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository).findByAuthenticationName("test user");
        verify(articleRepository, never()).findById(anyLong());
    }

    @DisplayName("해당 일기 ID가 없으면 ArticleNotFound 예외를 반환한다.")
    @Test
    void getArticle_ArticleNotFound() {
        // given
        User user = createUser();
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.of(user));

        given(articleRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleService.getArticle("test user", 1L))
                .isInstanceOf(ArticleNotFoundException.class);

        verify(userRepository).findByAuthenticationName("test user");
        verify(articleRepository).findById(1L);
    }

    @DisplayName("타 작성자의 게시글을 조회시에 UnauthorizedArticleRequestException이 반환된다.")
    @Test
    void getArticle_user_filtered() {
        // given
        User user = createUser();
        User otherUser = createUser();
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.of(user));

        Article otherUserArticle = createArticle(otherUser);
        given(articleRepository.findById(2L)).willReturn(Optional.of(otherUserArticle));

        // when
        assertThatThrownBy(() -> articleService.getArticle("test user", 2L))
                .isInstanceOf(UnauthorizedArticleRequestException.class);

        verify(userRepository).findByAuthenticationName("test user");
        verify(articleRepository).findById(2L);
    }

    @DisplayName("게시글 업데이트 요청시 게시글이 수정된다.")
    @Test
    void updateArticle() {
        // given
        User user = createUser();
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.of(user));

        Article article = createArticle(user);
        ArticleUpdateRequest updateRequest = ArticleUpdateRequest.builder()
                .content("Test Content")
                .spendDate("2024-04-04T11:22:33")
                .event("Test event")
                .thought("Test thought")
                .emotion("ANNOYED")
                .satisfaction(1F)
                .reason("Test Reason")
                .improvements("Test Improvements")
                .amount(100)
                .registerType("SAVE")
                .build();
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(article));

        // when
        articleService.updateArticle("test user", 1L, updateRequest);

        // then
        verify(userRepository).findByAuthenticationName("test user");
        verify(articleRepository).findById(1L);

        assertThat(article.getRegisterType()).isEqualTo(SAVE);
        assertThat(article.getSatisfaction()).isEqualTo(1F);
    }

    @DisplayName("게시글 ID를 입력하면 게시글을 삭제한다.")
    @Test
    void deleteArticle() {
        // given
        User user = createUser();
        given(userRepository.findByAuthenticationName(anyString())).willReturn(Optional.of(user));

        Article article = createArticle(user);
        given(articleRepository.findById(anyLong())).willReturn(Optional.of(article));

        // when
        articleService.deleteArticle("test user", 1L);

        // then
        verify(userRepository).findByAuthenticationName("test user");
        verify(articleRepository).findById(1L);
        verify(articleRepository).delete(article);

        assertThat(user.getArticles()).doesNotContain(article);
    }

    private User createUser() {
        return User.builder()
                .email("test@example.com")
                .mbti(ISTJ)
                .gender(MALE)
                .authenticationName("test user")
                .build();
    }

    private Article createArticle(User user) {
        return Article.builder()
                .user(user)
                .content("test content")
                .spendDate(LocalDateTime.of(2024, 5, 30, 0, 0))
                .event("test event")
                .thought("test thought")
                .emotion(ANNOYED)
                .satisfaction(5F)
                .reason(null)
                .improvements(null)
                .aiComment(null)
                .amount(100)
                .registerType(SPEND)
                .build();
    }

    private ArticleCreateRequest createArticleCreateRequest() {
        return ArticleCreateRequest.builder()
                .content("Test Content")
                .spendDate("2024-04-04T11:22:33")
                .event("Test event")
                .thought("Test thought")
                .emotion("ANNOYED")
                .satisfaction(5F)
                .reason("Test Reason")
                .improvements("Test Improvements")
                .amount(100)
                .registerType("SPEND")
                .build();
    }
}
