package com.example.spinlog.statistics.dto.wordanalysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@ToString
@Slf4j
public class Token {
    private String morph;
    private String pos;

    public static List<Token> createTokenList(MorphemeResponse response){
        List<MorphemeResponse.Sentence> sentences = response.getReturn_object().getSentence();

        return sentences.stream()
                .map(MorphemeResponse.Sentence::getMorp)
                .flatMap(List::stream)
                .map(m -> Token.builder()
                        .morph(m.getLemma())
                        .pos(m.getType())
                        .build())
                .toList();
    }
}
