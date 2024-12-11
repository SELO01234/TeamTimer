package com.example.application.team.repository;

import com.example.application.team.model.TeamMember;
import com.example.application.team.model.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Integer> {
    List<WorkingHours> getWorkingHoursByTeamMember(TeamMember teamMember);
}
