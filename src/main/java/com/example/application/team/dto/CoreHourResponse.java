package com.example.application.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoreHourResponse {
    private DayOfWeek dayOfWeek;
    private TimeInterval timeInterval;
}
