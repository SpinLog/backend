package com.example.spinlog.statistics.dto;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.user.entity.Gender;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class GenderEmotionAmountSumAndCountDto {
    private Gender gender;
    private Emotion emotion;
    private Long amountSum;
    private Long amountCount;
}
