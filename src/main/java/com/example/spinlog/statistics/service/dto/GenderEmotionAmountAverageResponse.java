package com.example.spinlog.statistics.service.dto;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.repository.dto.GenderEmotionAmountAverageDto;
import com.example.spinlog.user.entity.Gender;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class GenderEmotionAmountAverageResponse {
    private Gender gender;
    private List<EmotionAmountAverage> emotionAmountAverages;

    public static GenderEmotionAmountAverageResponse of(Gender gender, List<GenderEmotionAmountAverageDto> dtos){
        return GenderEmotionAmountAverageResponse.builder()
                .gender(gender)
                .emotionAmountAverages(
                        dtos.stream()
                                .map(EmotionAmountAverage::of)
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Override
    public String toString() {
        return "GenderEmotionAmountAverageResponse\n" +
                "gender=" + gender + "\n" +
                "emotionCount=\n" +
                emotionAmountAverages.stream()
                        .map(EmotionAmountAverage::toString)
                        .map(ea -> ea+"\n")
                        .toList()
                + "\n";
    }

    @Getter
    @Builder
    @EqualsAndHashCode
    @ToString
    public static class EmotionAmountAverage {
        private Emotion emotion;
        private Long amountAverage;

        public static EmotionAmountAverage of(GenderEmotionAmountAverageDto dto){
            return EmotionAmountAverage.builder()
                    .emotion(dto.getEmotion())
                    .amountAverage(dto.getAmountAverage())
                    .build();
        }
    }
}
