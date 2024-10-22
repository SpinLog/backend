package com.example.spinlog.statistics.dto.response;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.dto.repository.MBTIDailyAmountSumDto;
import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.dto.repository.MBTIEmotionAmountAverageDto;
import com.example.spinlog.user.entity.Mbti;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.roundForApiResponse;

@Getter
@Builder
public class MBTIEmotionAmountAverageResponse {
    private Mbti mbti;
    private List<MBTIEmotionAmountAverage> mbtiEmotionAmountAverages;

    public static MBTIEmotionAmountAverageResponse of(Mbti mbti, List<MBTIEmotionAmountAverageDto> dtosWithZeroPadding) {
        List<MBTIEmotionAmountAverage> list = dtosWithZeroPadding.stream()
                .map(e ->
                        new MBTIEmotionAmountAverageDto(
                                e.getMbtiFactor(),
                                e.getEmotion(),
                                roundForApiResponse(e.getAmountAverage())))
                .collect(
                        groupingBy(MBTIEmotionAmountAverageDto::getMbtiFactor))
                .entrySet().stream()
                .map((e) ->
                        MBTIEmotionAmountAverage.of(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(MBTIEmotionAmountAverage::getMbtiFactor))
                .toList();
        return MBTIEmotionAmountAverageResponse.builder()
                .mbti(mbti)
                .mbtiEmotionAmountAverages(list)
                .build();
    }

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
