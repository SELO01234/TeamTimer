package com.example.application.team.dto;

import com.example.application.team.model.TeamRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberRegister {

    private Integer userId;
    private TeamRole role;
}
