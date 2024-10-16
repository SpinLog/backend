package com.example.spinlog.statistics.service;

import com.example.spinlog.statistics.dto.wordanalysis.Token;
import com.example.spinlog.statistics.dto.wordanalysis.WordFrequency;
import com.example.spinlog.statistics.service.wordanalysis.MorphemeExtractionService;
import com.example.spinlog.statistics.service.wordanalysis.WordExtractionService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WordExtractionServiceTest {
    @InjectMocks
    WordExtractionService wordExtractionService;

    @Mock
    MorphemeExtractionService morphemeExtractionService;

    @Test
    @Order(1)
    void 파라미터를_그대로_BeforeApiService에게_넘긴다() throws Exception {
        // given
        List<String> memos = List.of("memo1", "memo2", "memo3");

        // when
        wordExtractionService.analyzeWords(memos);

        // then
        verify(morphemeExtractionService)
                .extractTokensFromMemos(eq(memos));
    }

    @Test
    @Order(2)
    void BeforeApiService로부터_토큰들을_받아_키워드만_추출한다() throws Exception {
        /**
         * 키워드 리스트(키워드 형태소)
         * NNG, NNP, NP, VV, VA, SL
         * */

        // given
        List<Token> keywords = List.of(
                new Token("단어1", "NNG"),
                new Token("단어2", "NNP"),
                new Token("단어3", "NP"),
                new Token("단어4", "SL")
        );
        List<Token> nonKeywords = List.of(
                new Token("단어6", "XXX"),
                new Token("단어7", "XXX")
        );
        List<Token> returned = Stream
                .concat(keywords.stream(), nonKeywords.stream())
                .toList();

        when(morphemeExtractionService.extractTokensFromMemos(any()))
                .thenReturn(returned);

        // when
        List<WordFrequency> results = wordExtractionService.analyzeWords(List.of());

        // then
        List<String> strings = results.stream()
                .map(WordFrequency::getWord)
                .toList();
        assertThat(strings)
                .containsExactlyInAnyOrderElementsOf(
                        keywords.stream()
                                .map(Token::getMorph)
                                .toList())
                .doesNotContainAnyElementsOf(
                        nonKeywords.stream()
                                .map(Token::getMorph)
                                .toList());
    }

    @Test
    @Order(3)
    void 키워드를_추출한_뒤_동사_형용사인_단어에_다_를_붙인다() throws Exception {
        /**
         * 동사: VV, 형용사: VA
         * */

        // given
        List<Token> keywords = List.of(
                new Token("단어1", "NNG"),
                new Token("단어2", "NNP"),
                new Token("단어3", "NP"),
                new Token("동사", "VV"),
                new Token("형용사", "VA")
        );

        when(morphemeExtractionService.extractTokensFromMemos(any()))
                .thenReturn(keywords);

        // when
        List<WordFrequency> results = wordExtractionService.analyzeWords(List.of());

        // then
        List<String> verbWordAndAdjectiveWord = keywords.stream()
                .filter(w -> w.getPos().startsWith("V"))
                .map(t -> t.getMorph() + "다")
                .toList();
        assertThat(results)
                .extracting(WordFrequency::getWord)
                .containsAnyElementsOf(verbWordAndAdjectiveWord);
    }

    @Test
    @Order(4)
    void 단어들의_빈도수를_세서_반환한다() throws Exception {
        // given
        List<Token> keywords = List.of(
                new Token("단어1", "NNG"),
                new Token("단어1", "NNG"),
                new Token("단어1", "NNG"),
                new Token("단어2", "NNP"),
                new Token("단어2", "NNP"),
                new Token("단어2", "NNP")
        );

        /*when(morphemeApiClient.extractTokensFromMemos(any()))
                .thenReturn(keywords);*/
        when(morphemeExtractionService.extractTokensFromMemos(any()))
                .thenReturn(keywords);

        // when
        List<WordFrequency> results = wordExtractionService.analyzeWords(List.of());

        // then
        List<String> wordList = keywords.stream()
                .map(Token::getMorph)
                .distinct()
                .toList();
        assertThat(results)
                .extracting(WordFrequency::getWord)
                .containsExactlyInAnyOrderElementsOf(wordList);

        assertThat(results)
                .extracting(WordFrequency::getFrequency)
                .allMatch((f) -> f == 3L);
    }

    @Test
    @Order(5)
    void 단어빈도수를_빈도수의_내림차순으로_상위_20개만_반환한다() {
        // Given
        List<String> memos = IntStream.range(0, 30)
                .mapToObj(i -> "word" + i)
                .collect(Collectors.toList());
        List<Token> tokens = memos.stream()
                .map(memo -> new Token(memo, "NNG"))
                .collect(Collectors.toList());

        when(morphemeExtractionService.extractTokensFromMemos(any()))
                .thenReturn(tokens);

        // When
        List<WordFrequency> actualWordFrequencies = wordExtractionService.analyzeWords(memos);

        // Then
        assertThat(actualWordFrequencies).hasSize(20);
        assertThat(isSortedDescending(actualWordFrequencies)).isTrue();
    }

    private boolean isSortedDescending(List<WordFrequency> wordFrequencies) {
        return IntStream.range(0, wordFrequencies.size() - 1)
                .allMatch(i -> wordFrequencies.get(i).getFrequency() >= wordFrequencies.get(i + 1).getFrequency());
    }
}