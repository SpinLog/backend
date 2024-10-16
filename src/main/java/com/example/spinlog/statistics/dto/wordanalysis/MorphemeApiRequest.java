package com.example.spinlog.statistics.dto.wordanalysis;

import lombok.*;

@Builder
@Getter
@EqualsAndHashCode
public class MorphemeApiRequest {

    private final String request_id;
    private final Argument argument;

    @Getter
    @Builder
    @EqualsAndHashCode
    public static class Argument {
        private final String analysis_code = "morp";
        private final String text;

        public Argument(String text) {
            this.text = text;
        }
    }
}
