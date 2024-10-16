package com.example.spinlog.statistics.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class MemoDto {
    private String content;
    private String event;
    private String thought;
    private String reason;
    private String improvements;
}
