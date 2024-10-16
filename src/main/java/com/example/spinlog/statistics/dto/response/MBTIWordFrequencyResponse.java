package com.example.spinlog.statistics.dto.response;

import com.example.spinlog.statistics.dto.wordanalysis.WordFrequency;
import com.example.spinlog.user.entity.Mbti;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MBTIWordFrequencyResponse {
    private Mbti mbti;
    private List<WordFrequency> allWordFrequencies;
    private List<WordFrequency> userWordFrequencies;
}
