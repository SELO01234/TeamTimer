package com.example.application.team.repository;

import com.example.application.team.model.Team;
import com.example.application.team.model.TeamMember;
import com.example.application.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Integer> {

    boolean existsByUser(User user);

    boolean existsByTeam(Team team);

    Optional<List<TeamMember>> findAllByTeam(Team team);
}
