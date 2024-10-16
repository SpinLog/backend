package com.example.spinlog.statistics.dto.response;

import com.example.spinlog.statistics.dto.repository.GenderDailyAmountSumDto;
import com.example.spinlog.user.entity.Gender;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class GenderDailyAmountSumResponse {
    private Gender gender;
    private List<DailyAmountSum> dailyAmountSums;

    public static GenderDailyAmountSumResponse of(Gender gender, List<GenderDailyAmountSumDto> dtos){
        return GenderDailyAmountSumResponse.builder()
                .gender(gender)
                .dailyAmountSums(
                        dtos.stream()
                                .map(DailyAmountSum::of)
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Override
    public String toString() {
        return "EmotionAmountAverage\n" +
                "gender=" + gender + "\n" +
                "emotionCount=\n" +
                dailyAmountSums.stream()
                        .map(DailyAmountSum::toString)
                        .map(ea -> ea+"\n")
                        .toList()
                + "\n";
    }

    @Getter
    @ToString
    public static class DailyAmountSum {
        private LocalDate date;
        private Long amountSum;

        @Builder
        public DailyAmountSum(LocalDate date, Long amountSum) {
            this.date = date;
            this.amountSum = amountSum;
        }

        public static DailyAmountSum of(GenderDailyAmountSumDto dto){
            return DailyAmountSum.builder()
                    .date(dto.getLocalDate())
                    .amountSum(dto.getAmountSum())
                    .build();
        }
    }
}
