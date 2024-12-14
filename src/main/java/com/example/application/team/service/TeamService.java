package com.example.application.team.service;

import com.example.application.team.dto.TeamMemberRegister;
import com.example.application.team.dto.TeamMemberResponse;
import com.example.application.team.model.Team;

import java.util.List;

public interface TeamService {

    void createTeam(Team team);

    void deleteTeam(Integer teamId);

    Team updateTeam(Integer teamId, Team team);

    void addTeamMember(Integer teamId, TeamMemberRegister teamMemberRegister);

    List<TeamMemberResponse> getTeamMembers(Integer teamId);

    void assignTeamLeader(Integer teamId, Integer memberId);

    void deleteTeamMember(Integer teamId, Integer memberId);
}
