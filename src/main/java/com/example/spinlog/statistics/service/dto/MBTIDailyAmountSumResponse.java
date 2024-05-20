package com.example.spinlog.statistics.service.dto;

import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.repository.dto.MBTIDailyAmountSumDto;
import com.example.spinlog.user.entity.Mbti;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class MBTIDailyAmountSumResponse {
    private Mbti mbti;
    private List<MBTIDailyAmountSum> mbtiDailyAmountSums;

    @Getter
    @Builder
    public static class MBTIDailyAmountSum {
        private MBTIFactor mbtiFactor;
        private List<DailyAmountSum> dailyAmountSums;

        public static MBTIDailyAmountSum of(MBTIFactor factor, List<MBTIDailyAmountSumDto> dtos) {
            return MBTIDailyAmountSum.builder()
                    .mbtiFactor(factor)
                    .dailyAmountSums(
                            dtos.stream()
                                    .map(DailyAmountSum::of)
                                    .collect(Collectors.toList())
                    )
                    .build();
        }
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    public static class DailyAmountSum {
        private LocalDate date;
        private Long amountSum;

        @Builder
        public DailyAmountSum(LocalDate date, Long amountSum) {
            this.date = date;
            this.amountSum = amountSum;
        }

        public static DailyAmountSum of(MBTIDailyAmountSumDto dto){
            return DailyAmountSum.builder()
                    .date(dto.getLocalDate())
                    .amountSum(dto.getAmountSum())
                    .build();
        }
    }
}
