package com.example.spinlog.statistics.dto;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.entity.MBTIFactor;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class MBTIEmotionAmountSumAndCountDto {
    private MBTIFactor mbtiFactor;
    private Emotion emotion;
    private Long amountSum;
    private Long amountCount;
}
