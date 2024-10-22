package com.example.spinlog.statistics.dto.response;

import com.example.spinlog.statistics.entity.MBTIFactor;
import com.example.spinlog.statistics.dto.repository.MBTIDailyAmountSumDto;
import com.example.spinlog.user.entity.Mbti;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Getter
@Builder
public class MBTIDailyAmountSumResponse {
    private Mbti mbti;
    private List<MBTIDailyAmountSum> mbtiDailyAmountSums;

    public static MBTIDailyAmountSumResponse of(Mbti mbti, List<MBTIDailyAmountSumDto> dtosWithZeroPadding) {
        return MBTIDailyAmountSumResponse.builder()
                .mbti(mbti)
                .mbtiDailyAmountSums(
                        dtosWithZeroPadding.stream()
                                .collect(
                                        groupingBy(MBTIDailyAmountSumDto::getMbtiFactor))
                                .entrySet().stream()
                                .map((e) ->
                                        MBTIDailyAmountSumResponse.MBTIDailyAmountSum.of(e.getKey(), e.getValue()))
                                .sorted(Comparator.comparing(MBTIDailyAmountSumResponse.MBTIDailyAmountSum::getMbtiFactor))
                                .toList())
                .build();
    }

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
