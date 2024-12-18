package com.example.application.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventScheduleResponse {
    private List<WorkingHoursDTO> workingHours;
    private List<EventResponseDTO> events;
}
