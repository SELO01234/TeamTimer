package com.example.application.util;

import com.example.application.team.dto.WorkingHoursDTO;

import java.time.*;

public final class TimeConverter {

    public static WorkingHoursDTO convertTimeToZonedTime(LocalDateTime startTime, LocalDateTime endTime, String timezone, DayOfWeek dayOfWeek){

        ZoneId zoneId = ZoneId.of(timezone);

        ZonedDateTime startDateZonedTime = ZonedDateTime.of(startTime, ZoneOffset.UTC);
        ZonedDateTime endDateZonedTime = ZonedDateTime.of(endTime,ZoneOffset.UTC);

        LocalDateTime startLocalDateTime = startDateZonedTime.withZoneSameInstant(zoneId).toLocalDateTime();
        LocalDateTime endLocalDateTime = endDateZonedTime.withZoneSameInstant(zoneId).toLocalDateTime();
        if(startTime.toLocalDate().isBefore(startDateZonedTime.toLocalDate()))
        {
            dayOfWeek = DayOfWeek.of((dayOfWeek.getValue() - 1) % 7);
        }
        else if (startTime.toLocalDate().isAfter(startDateZonedTime.toLocalDate()))
        {
            dayOfWeek = DayOfWeek.of((dayOfWeek.getValue() + 1) % 7);
        }

        return WorkingHoursDTO
                .builder()
                .dayOfWeek(dayOfWeek)
                .startTime(startLocalDateTime)
                .endTime(endLocalDateTime)
                .build();
    }
}
