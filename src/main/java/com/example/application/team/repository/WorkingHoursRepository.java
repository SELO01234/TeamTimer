package com.example.application.team.repository;

import com.example.application.team.dto.TeamMemberResponse;
import com.example.application.team.model.TeamMember;
import com.example.application.team.model.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Integer> {
    Optional<List<WorkingHours>> getWorkingHoursByTeamMember(TeamMember teamMember);
}
