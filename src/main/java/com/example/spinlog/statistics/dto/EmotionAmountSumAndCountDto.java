package com.example.spinlog.statistics.dto;

import com.example.spinlog.article.entity.Emotion;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class EmotionAmountSumAndCountDto {
    private Emotion emotion;
    private Long amountSum;
    private Long amountCount;
}
