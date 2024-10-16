package com.example.spinlog.statistics.dto.response;

import com.example.spinlog.statistics.dto.wordanalysis.WordFrequency;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GenderWordFrequencyResponse {
    private List<WordFrequency> maleWordFrequencies;
    private List<WordFrequency> femaleWordFrequencies;
}
