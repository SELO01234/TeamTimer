package com.example.application.team.service;

import com.example.application.team.dto.TeamMemberRegister;
import com.example.application.team.dto.TeamMemberResponse;
import com.example.application.team.model.Team;
import com.example.application.team.model.TeamMember;
import com.example.application.team.model.TeamRole;
import com.example.application.team.repository.TeamMemberRepository;
import com.example.application.team.repository.TeamRepository;
import com.example.application.user.model.Role;
import com.example.application.user.model.User;
import com.example.application.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
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
    @Transactional
    public void deleteTeam(Integer teamId) throws RuntimeException {
        // Check whether team is present or not
        if(teamRepository.findById(teamId).isEmpty()){
            throw new RuntimeException("Team is not present");
        }

        //delete
        teamRepository.deleteById(teamId);
    }

    @Override
    @Transactional
    public Team updateTeam(Integer teamId, Team team) throws RuntimeException {

        //get the present team
        Team newTeam = teamRepository.findById(teamId).orElseThrow(()-> new RuntimeException("Team is not present"));
        newTeam.setName(team.getName());
        newTeam.setDescription(team.getDescription());

        //update
        teamRepository.save(newTeam);

        //return updated team
        return teamRepository.findById(teamId).orElseThrow(()-> new RuntimeException("Could not implement update operation"));
    }

    @Override
    public void addTeamMember(Integer teamId, TeamMemberRegister teamMemberRegister) throws RuntimeException {

        //get user and team
        Team team = teamRepository.findById(teamId).orElseThrow(()-> new RuntimeException("Team is not present"));
        User user = userRepository.findById(teamMemberRegister.getUserId()).orElseThrow(()-> new RuntimeException("User is not present"));

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
    public List<TeamMemberResponse> getTeamMembers(Integer teamId) throws RuntimeException {
        //check whether team exists
        if(!teamRepository.existsById(teamId)){
            throw new RuntimeException("Team is not present");
        }

        //return members
        List<Object[]> results = teamMemberRepository.findAllByTeamId(teamId);
        return results.stream().map(this::mapToTeamMemberResponse).toList();
    }

    @Override
    public void assignTeamLeader(Integer teamId, Integer memberId) throws RuntimeException {
        //check whether team exists
        if(!teamRepository.existsById(teamId)){
            throw new RuntimeException("Team is not present");
        }

        //check whether user exists
        if(!teamMemberRepository.existsById(memberId)){
            throw new RuntimeException("Team member is not present");
        }

        //get team member
        TeamMember teamMember = teamMemberRepository.findById(memberId).orElseThrow(RuntimeException::new);

        //assign leader role to the team member
        teamMember.setRoleInTeam(TeamRole.LEADER);

        //update the team member
        teamMemberRepository.save(teamMember);
    }

    private TeamMemberResponse mapToTeamMemberResponse(Object[] result) {
        return TeamMemberResponse.builder()
                .teamMemberId((Integer) result[0])
                .roleInTeam(TeamRole.valueOf((String) result[1]))
                .username((String) result[2])
                .email((String) result[3])
                .role(Role.valueOf((String) result[4]))
                .timezone(((Instant) result[5]).atOffset(ZoneOffset.UTC)) // Convert Instant to OffsetDateTime
                .teamName((String) result[6])
                .build();
    }
}
