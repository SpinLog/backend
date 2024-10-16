package com.example.spinlog.statistics.dto.repository;

import com.example.spinlog.article.entity.Emotion;
import com.example.spinlog.statistics.entity.MBTIFactor;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class MBTIEmotionAmountAverageDto {
    private MBTIFactor mbtiFactor;
    private Emotion emotion;
    private Long amountAverage;
}
