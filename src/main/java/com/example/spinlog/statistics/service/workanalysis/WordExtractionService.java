package com.example.spinlog.statistics.service.workanalysis;

import com.example.spinlog.statistics.service.dto.Token;
import com.example.spinlog.statistics.service.dto.WordFrequency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordExtractionService {
    private final MorphemeExtractionService morphemeExtractionService;
    private final int MAX_SIZE = 20;

    public List<WordFrequency> analyzeWords(List<String> memos){
        List<Token> tokens = morphemeExtractionService.extractTokensFromMemos(memos);
        List<String> keywords = filterAndModifyKeywords(tokens);
        List<WordFrequency> wordFrequencies = groupByFrequency(keywords);

        // 단어 빈도수를 빈도수 내림차순으로 정렬해서 상위 20개만 반환한다.
        return wordFrequencies.stream()
                .sorted(Comparator.comparingLong(WordFrequency::getFrequency).reversed())
                .limit(MAX_SIZE)
                .toList();
    }

    private List<String> filterAndModifyKeywords(List<Token> tokens) {
        return tokens.stream()
                .filter(this::isKeyword)
                .map(this::getMorphWithSuffix)
                .collect(Collectors.toList());
    }

    private List<WordFrequency> groupByFrequency(List<String> keywords) {
        return keywords.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .map(this::createWordFrequency)
                .toList();
    }

    private WordFrequency createWordFrequency(Map.Entry<String, Long> entry) {
        return WordFrequency.builder()
                .word(entry.getKey())
                .frequency(entry.getValue())
                .build();
    }

    private String getMorphWithSuffix(Token token) {
        return isVerbOrAdjective(token) ? token.getMorph().concat("다") : token.getMorph();
    }

    private boolean isKeyword(Token token){
        return token.getPos().matches("NNG|NNP|NP|VV|VA|SL");
    }

    private boolean isVerbOrAdjective(Token token) {
        return token.getPos().startsWith("V");
    }
}
