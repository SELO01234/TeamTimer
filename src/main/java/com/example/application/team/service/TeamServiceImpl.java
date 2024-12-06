package com.example.application.team.service;

import com.example.application.team.model.Team;
import com.example.application.team.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeamServiceImpl implements TeamService{

    private final TeamRepository teamRepository;

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository){
        this.teamRepository = teamRepository;
    }

    @Override
    public void createTeam(Team team) {
        teamRepository.save(team);
    }

    @Override
    public void deleteTeam(Integer teamId) throws RuntimeException {
        // Check whether team is present or not
        if(teamRepository.findById(teamId).isEmpty()){
            throw new RuntimeException("Team is not present");
        }

        //delete
        teamRepository.deleteById(teamId);
    }

    @Override
    public Team updateTeam(Integer teamId, Team team) throws RuntimeException {
        // Check whether team is present or not
        if(teamRepository.findById(teamId).isEmpty()){
            throw new RuntimeException("Team is not present");
        }

        //get the present team
        Team newTeam = teamRepository.findById(teamId).orElseThrow(()-> new RuntimeException("Could not implement update operation"));
        newTeam.setName(team.getName());
        newTeam.setDescription(team.getDescription());

        //update
        teamRepository.save(newTeam);

        //return updated team
        return teamRepository.findById(teamId).orElseThrow(()-> new RuntimeException("Could not implement update operation"));
    }
}
