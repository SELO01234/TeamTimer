package com.example.application.util;

import com.example.application.team.dto.TimeOffRequestDTO;
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
        if(startLocalDateTime.toLocalTime().isAfter(endLocalDateTime.toLocalTime()))
        {
            endLocalDateTime = endLocalDateTime.plusDays(1);
        }

        return WorkingHoursDTO
                .builder()
                .dayOfWeek(dayOfWeek)
                .startTime(startLocalDateTime)
                .endTime(endLocalDateTime)
                .build();
    }

    public static WorkingHoursDTO convertZonedTimeToUtcTime(LocalDateTime startTime, LocalDateTime endTime, String timezone, DayOfWeek dayOfWeek){

        ZoneId zoneId = ZoneId.of(timezone);

        ZonedDateTime startDateZonedTime = ZonedDateTime.of(startTime, zoneId);
        ZonedDateTime endDateZonedTime = ZonedDateTime.of(endTime, zoneId);

        LocalDateTime startLocalDateTime = startDateZonedTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endLocalDateTime = endDateZonedTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        if(startTime.toLocalDate().isBefore(startDateZonedTime.toLocalDate()))
        {
            dayOfWeek = DayOfWeek.of((dayOfWeek.getValue() - 1) % 7);
        }
        else if (startTime.toLocalDate().isAfter(startDateZonedTime.toLocalDate()))
        {
            dayOfWeek = DayOfWeek.of((dayOfWeek.getValue() + 1) % 7);
        }
        if(startLocalDateTime.isAfter(endLocalDateTime))
        {
            endLocalDateTime = endLocalDateTime.plusDays(1);
        }

        return WorkingHoursDTO
                .builder()
                .dayOfWeek(dayOfWeek)
                .startTime(startLocalDateTime)
                .endTime(endLocalDateTime)
                .build();
    }

    public static TimeOffRequestDTO convertZonedTimeToUtcTimeTimeOffRequest(TimeOffRequestDTO timeOffRequestDTO, String timezone){

        ZoneId zoneId = ZoneId.of(timezone);

        ZonedDateTime startDateZonedTime = ZonedDateTime.of(timeOffRequestDTO.getStartDate(), zoneId);
        ZonedDateTime endDateZonedTime = ZonedDateTime.of(timeOffRequestDTO.getEndDate(), zoneId);

        LocalDateTime startLocalDateTime = startDateZonedTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime endLocalDateTime = endDateZonedTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        return TimeOffRequestDTO
                .builder()
                .startDate(startLocalDateTime)
                .endDate(endLocalDateTime)
                .reason(timeOffRequestDTO.getReason())
                .approved(timeOffRequestDTO.isApproved())
                .build();
    }


    public static TimeOffRequestDTO convertTimeToZonedTimeTimeOffRequest(TimeOffRequestDTO timeOffRequestDTO, String timezone){

        ZoneId zoneId = ZoneId.of(timezone);

        ZonedDateTime startDateZonedTime = ZonedDateTime.of(timeOffRequestDTO.getStartDate(), ZoneOffset.UTC);
        ZonedDateTime endDateZonedTime = ZonedDateTime.of(timeOffRequestDTO.getEndDate(),ZoneOffset.UTC);

        LocalDateTime startLocalDateTime = startDateZonedTime.withZoneSameInstant(zoneId).toLocalDateTime();
        LocalDateTime endLocalDateTime = endDateZonedTime.withZoneSameInstant(zoneId).toLocalDateTime();

        return TimeOffRequestDTO
                .builder()
                .startDate(startLocalDateTime)
                .endDate(endLocalDateTime)
                .reason(timeOffRequestDTO.getReason())
                .approved(timeOffRequestDTO.isApproved())
                .build();
    }
}
