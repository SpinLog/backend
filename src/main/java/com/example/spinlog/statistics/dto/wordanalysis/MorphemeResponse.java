package com.example.spinlog.statistics.dto.wordanalysis;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MorphemeResponse {
    private String request_id;
    private int result;
    private ReturnObject return_object;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReturnObject {
        private List<Sentence> sentence;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Sentence {
        private List<Morpheme> morp;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Morpheme {
        private String lemma;
        private String type;
    }
}