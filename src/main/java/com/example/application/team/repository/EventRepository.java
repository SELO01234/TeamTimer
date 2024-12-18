package com.example.application.team.repository;

import com.example.application.team.model.Event;
import com.example.application.team.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {

    Optional<Event> findByTeam(Team team);
}
