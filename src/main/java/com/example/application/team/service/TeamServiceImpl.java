package com.example.application.team.service;

import com.example.application.team.dto.TeamMemberRegister;
import com.example.application.team.model.Team;
import com.example.application.team.model.TeamMember;
import com.example.application.team.repository.TeamMemberRepository;
import com.example.application.team.repository.TeamRepository;
import com.example.application.user.model.User;
import com.example.application.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService{

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    private final TeamMemberRepository teamMemberRepository;

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository, UserRepository userRepository, TeamMemberRepository teamMemberRepository){
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.teamMemberRepository = teamMemberRepository;
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

    @Override
    public void addTeamMember(Integer teamId, TeamMemberRegister teamMemberRegister) throws RuntimeException {
        //check whether team exists
        if(!teamRepository.existsById(teamId)){
            throw new RuntimeException("Team is not present");
        }

        //check whether user exists
        if(!userRepository.existsById(teamMemberRegister.getUserId())){
            throw new RuntimeException("User is not present");
        }

        //get user and team
        Team team = teamRepository.findById(teamId).orElseThrow(RuntimeException::new);
        User user = userRepository.findById(teamMemberRegister.getUserId()).orElseThrow(RuntimeException::new);

        //check if user is already in team or not
        if(teamMemberRepository.existsByUser(user) && teamMemberRepository.existsByTeam(team)){
            throw new RuntimeException("User is already in this team");
        }

        //create team member object and save
        teamMemberRepository.save(
                TeamMember
                        .builder()
                        .user(user)
                        .team(team)
                        .roleInTeam(teamMemberRegister.getRole())
                        .build()
        );
    }

    @Override
    public List<TeamMember> getTeamMembers(Integer teamId) throws RuntimeException {
        //check whether team exists
        if(!teamRepository.existsById(teamId)){
            throw new RuntimeException("Team is not present");
        }

        //get team
        Team team = teamRepository.findById(teamId).orElseThrow(RuntimeException::new);

        //return members
        return teamMemberRepository.findAllByTeam(team).orElseThrow(RuntimeException::new);
    }
}
