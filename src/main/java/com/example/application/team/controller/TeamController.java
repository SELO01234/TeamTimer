package com.example.application.team.controller;

import com.example.application.team.dto.TeamMemberRegister;
import com.example.application.team.model.Team;
import com.example.application.team.model.TeamMember;
import com.example.application.team.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService){
        this.teamService = teamService;
    }

    @PostMapping("/createTeam")
    public ResponseEntity<String> createTeam(@RequestBody Team team){
        teamService.createTeam(team);
        return ResponseEntity.ok().body("Team has created");
    }

    @DeleteMapping("/delete/{teamId}")
    public ResponseEntity<String> deleteTeam(@PathVariable("teamId") Integer teamId){
        teamService.deleteTeam(teamId);
        return ResponseEntity.ok().body("Team has deleted successfully");
    }

    @PutMapping("/update/{teamId}")
    public ResponseEntity<Team> updateTeam(@PathVariable("teamId") Integer teamId, @RequestBody Team team){
        return ResponseEntity.ok().body(teamService.updateTeam(teamId, team));
    }

    @PostMapping("/{teamId}/members-add")
    public ResponseEntity<String> addTeamMember(@PathVariable("teamId") Integer teamId, @RequestBody TeamMemberRegister teamMemberRegister){
        teamService.addTeamMember(teamId, teamMemberRegister);
        return ResponseEntity.ok().body("User has added to the team");
    }

    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<TeamMember>> getTeamMembers(@PathVariable("teamId") Integer teamId){
        return ResponseEntity.ok().body(teamService.getTeamMembers(teamId));
    }
}
