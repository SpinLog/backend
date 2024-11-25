package com.example.spinlog.utils;

import java.time.*;

public class ClockUtils {
    public static Clock getFixedClock(String localDate){
        String now = LocalDate.now().toString();
        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.parse(localDate), LocalTime.now());
        ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return Clock.fixed(
                zonedDateTime.toInstant(),
                ZoneId.systemDefault());
    }
}
