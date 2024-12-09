package com.example.application.team.service;

import com.example.application.team.dto.WorkingHoursRequest;
import com.example.application.team.model.TeamMember;
import com.example.application.team.model.WorkingHours;
import com.example.application.team.repository.TeamMemberRepository;
import com.example.application.team.repository.WorkingHoursRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
public class WorkingHoursServiceImpl implements WorkingHoursService{

    private final WorkingHoursRepository workingHoursRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Autowired
    public WorkingHoursServiceImpl(WorkingHoursRepository workingHoursRepository, TeamMemberRepository teamMemberRepository) {
        this.workingHoursRepository = workingHoursRepository;
        this.teamMemberRepository = teamMemberRepository;
    }

    @Transactional
    @Override
    public void setDailyWorkingHours(Integer teamId, Integer memberId, List<WorkingHoursRequest> workingHoursRequest) throws RuntimeException {
        //Get user's timezone from team member
        String timezone = teamMemberRepository.findTimezoneByTeamAndMemberId(teamId, memberId).orElseThrow(()-> new RuntimeException("User is not present"));

        if(timezone == null){
            throw new RuntimeException("Timezone is not set");
        }

        ZoneId zoneId = ZoneId.of(timezone);

        //get teamMember instance
        TeamMember teamMember = teamMemberRepository.findById(memberId).orElseThrow(()-> new RuntimeException("Team member is not present"));

        //process each working hour request
        workingHoursRequest.forEach(request -> {
            //convert time to utc
            ZonedDateTime startDateZonedTime = request.getStartTime().atDate(LocalDate.now()).atZone(zoneId);
            ZonedDateTime endDateZonedTime = request.getEndTime().atDate(LocalDate.now()).atZone(zoneId);

            LocalTime utcStartTime = startDateZonedTime.withZoneSameInstant(ZoneOffset.UTC).toLocalTime();
            LocalTime utcEndTime = endDateZonedTime.withZoneSameInstant(ZoneOffset.UTC).toLocalTime();

            //save to repository
            workingHoursRepository.save(
                    WorkingHours
                            .builder()
                            .dayOfWeek(request.getDayOfWeek())
                            .startTime(utcStartTime)
                            .endTime(utcEndTime)
                            .teamMember(teamMember)
                            .build()
            );
        });
    }
}
