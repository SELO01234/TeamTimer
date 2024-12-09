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

    @Query(value = "SELECT m.team_member_id, m.role_in_team, u.username, u.email, u.role, u.timezone, t.name AS team_name FROM team_member m " +
            "JOIN _user u ON m.user_id = u.id " +
            "JOIN team t ON t.id=m.team_id " +
            "WHERE m.team_id=:team_id ;", nativeQuery = true)
    List<Object[]> findAllByTeamId(@Param("team_id")Integer teamId);

    @Query(value = "SELECT u.timezone FROM team_member t JOIN _user u ON u.id=t.user_id WHERE t.team_id=:team_id AND t.team_member_id=:team_member_id", nativeQuery = true)
    Optional<String> findTimezoneByTeamAndMemberId(@Param("team_id")Integer teamId,@Param("team_member_id") Integer memberId);
}
