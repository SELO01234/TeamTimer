package com.example.application.holiday.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer holidayId;

    private String regionCode;

    private String holidayName;

    private LocalDate date;
}
