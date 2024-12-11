package com.example.application.team.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class WorkingHours {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false, columnDefinition = "TIME")
    private LocalDateTime startTime;

    @Column(nullable = false, columnDefinition = "TIME")
    private LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "team_member_id")
    private TeamMember teamMember;
}
