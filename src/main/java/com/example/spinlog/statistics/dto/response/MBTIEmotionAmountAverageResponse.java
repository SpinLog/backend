package com.example.spinlog.statistics.dto.response;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.dto.MBTIEmotionAmountAverageDto;
import com.example.spinlog.user.entity.Mbti;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MBTIEmotionAmountAverageResponse {
    private Mbti mbti;
    private List<MBTIEmotionAmountAverage> mbtiEmotionAmountAverages;

    @Getter
    @Builder
    public static class MBTIEmotionAmountAverage {
        private MBTIFactor mbtiFactor;
        private List<EmotionAmountAverage> emotionAmountAverages;

        public static MBTIEmotionAmountAverage of(MBTIFactor factor, List<MBTIEmotionAmountAverageDto> dtos) {
            return MBTIEmotionAmountAverage.builder()
                    .mbtiFactor(factor)
                    .emotionAmountAverages(
                            dtos.stream()
                                    .map(EmotionAmountAverage::of)
                                    .collect(Collectors.toList())
                    )
                    .build();
        }
    }

    @Getter
    @EqualsAndHashCode
    public static class EmotionAmountAverage {
        private Emotion emotion;
        private Long amountAverage;

        @Builder
        public EmotionAmountAverage(Emotion emotion, Long average) {
            this.emotion = emotion;
            this.amountAverage = average;
        }

        public static EmotionAmountAverage of(MBTIEmotionAmountAverageDto dto){
            return EmotionAmountAverage.builder()
                    .emotion(dto.getEmotion())
                    .average(dto.getAmountAverage())
                    .build();
        }
    }
}
