package com.example.application.team.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeOffRequestDTO {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reason;
    private boolean approved;
}
