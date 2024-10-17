package com.example.spinlog.statistics.dto.wordanalysis;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WordFrequency{
    private String word;
    private Long frequency;
}
