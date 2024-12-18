package com.example.application.team.repository;

import com.example.application.team.model.Event;
import com.example.application.team.model.Team;
import com.example.application.team.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {

    Optional<List<Event>> findByTeam(Team team);

    Optional<List<Event>> findByCreatedBy(TeamMember teamMember);
}
