package com.example.spinlog.statistics.dto.response;

import com.example.spinlog.statistics.dto.repository.MBTISatisfactionAverageDto;
import com.example.spinlog.user.entity.Mbti;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static com.example.spinlog.statistics.utils.StatisticsCacheUtils.roundForApiResponse;

@Getter
@Builder
public class MBTISatisfactionAverageResponse {
    private Mbti mbti;
    private List<MBTISatisfactionAverageDto> mbtiSatisfactionAverages;

    public static MBTISatisfactionAverageResponse of(Mbti mbti, List<MBTISatisfactionAverageDto> dtos) {
        return MBTISatisfactionAverageResponse.builder()
                .mbti(mbti)
                .mbtiSatisfactionAverages(
                        dtos.stream()
                                .map(dto -> MBTISatisfactionAverageDto.builder()
                                        .mbtiFactor(dto.getMbtiFactor())
                                        .satisfactionAverage(roundForApiResponse(dto.getSatisfactionAverage()))
                                        .build())
                                .toList()
                )
                .build();
    }
}
