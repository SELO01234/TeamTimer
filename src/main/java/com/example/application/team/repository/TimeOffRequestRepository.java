package com.example.application.team.repository;

import com.example.application.team.dto.TimeOffRequestDTO;
import com.example.application.team.model.TeamMember;
import com.example.application.team.model.TimeOffRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimeOffRequestRepository extends JpaRepository<TimeOffRequest, Integer> {
    List<TimeOffRequest> getAllByTeamMember(TeamMember teamMember);
}
