package com.example.application.team.dto;

import com.example.application.team.model.TeamRole;
import com.example.application.user.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {

    private Integer teamMemberId;
    private TeamRole roleInTeam;
    private String username;
    private String email;
    private Role role;
    private OffsetDateTime timezone;
    private String teamName;
}
