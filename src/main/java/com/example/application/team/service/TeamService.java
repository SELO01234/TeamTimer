package com.example.application.team.service;

import com.example.application.team.model.Team;

public interface TeamService {

    void createTeam(Team team);

    void deleteTeam(Integer teamId);

    Team updateTeam(Integer teamId, Team team);
}
