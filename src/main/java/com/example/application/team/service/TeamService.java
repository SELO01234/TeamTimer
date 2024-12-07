package com.example.application.team.service;

import com.example.application.team.dto.TeamMemberRegister;
import com.example.application.team.model.Team;
import com.example.application.team.model.TeamMember;

import java.util.List;

public interface TeamService {

    void createTeam(Team team);

    void deleteTeam(Integer teamId);

    Team updateTeam(Integer teamId, Team team);

    void addTeamMember(Integer teamId, TeamMemberRegister teamMemberRegister);

    List<TeamMember> getTeamMembers(Integer teamId);
}
