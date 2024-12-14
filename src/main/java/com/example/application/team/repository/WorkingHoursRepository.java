package com.example.application.team.repository;

import com.example.application.team.model.TeamMember;
import com.example.application.team.model.WorkingHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Integer> {
    Optional<List<WorkingHours>> getWorkingHoursByTeamMember(TeamMember teamMember);

    @Query(value = "SELECT id, day_of_week, start_time, end_time, w.team_member_id FROM working_hours w " +
            "JOIN team_member tm " +
            "ON w.team_member_id=tm.team_member_id WHERE tm.team_id=:team_id AND day_of_week=:day_of_week ORDER BY team_member_id",
            nativeQuery = true)
    List<WorkingHours> getWorkingHoursByTeamIdAndDay(@Param("team_id")Integer teamId, @Param("day_of_week")String dayOfWeek);
}
