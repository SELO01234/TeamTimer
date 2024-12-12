package com.example.application.team.service;

import com.example.application.team.dto.TeamMemberResponse;
import com.example.application.team.dto.WorkingHoursDTO;
import com.example.application.team.model.TeamMember;
import com.example.application.team.model.WorkingHours;
import com.example.application.team.repository.TeamMemberRepository;
import com.example.application.team.repository.TeamRepository;
import com.example.application.team.repository.WorkingHoursRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkingHoursServiceImpl implements WorkingHoursService{

    private final WorkingHoursRepository workingHoursRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamServiceImpl teamService;
    private final TeamRepository teamRepository;

    @Autowired
    public WorkingHoursServiceImpl(WorkingHoursRepository workingHoursRepository,
                                   TeamMemberRepository teamMemberRepository,
                                   TeamServiceImpl teamService,
                                   TeamRepository teamRepository) {
        this.workingHoursRepository = workingHoursRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamService = teamService;
        this.teamRepository = teamRepository;
    }

    @Transactional
    @Override
    public void setDailyWorkingHours(Integer teamId, Integer memberId, List<WorkingHoursDTO> workingHoursDTO) throws RuntimeException {
        //Get user's timezone from team member
        String timezone = teamMemberRepository.findTimezoneByTeamAndMemberId(teamId, memberId).orElseThrow(()-> new RuntimeException("User is not present"));

        if(timezone == null){
            throw new RuntimeException("Timezone is not set");
        }

        ZoneId zoneId = ZoneId.of(timezone);

        //get teamMember instance
        TeamMember teamMember = teamMemberRepository.findById(memberId).orElseThrow(()-> new RuntimeException("Team member is not present"));

        //process each working hour request
        workingHoursDTO.forEach(request -> {
            //convert time to utc
            ZonedDateTime startDateZonedTime = ZonedDateTime.of(request.getStartTime(),zoneId);
            ZonedDateTime endDateZonedTime = ZonedDateTime.of(request.getEndTime(),zoneId);

            LocalDateTime utcStartTime = startDateZonedTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
            LocalDateTime utcEndTime = endDateZonedTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

            System.out.println(utcStartTime);
            System.out.println(startDateZonedTime);
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

    @Override
    public List<WorkingHoursDTO> getDailyWorkingHours(Integer teamId, Integer memberId) throws RuntimeException {

        //Get Team Member
        TeamMember teamMember = teamMemberRepository.findById(memberId).orElseThrow(()-> new RuntimeException("Team member is not present"));
        List<WorkingHours> workingHours = workingHoursRepository.getWorkingHoursByTeamMember(teamMember).orElseThrow(()-> new RuntimeException("Working Hours is not present"));

        List<WorkingHoursDTO> workingHoursDTOS = new ArrayList<>();
        workingHours.forEach(workingHour -> {
            System.out.println(workingHour.getStartTime());
            workingHoursDTOS.add(
                    WorkingHoursDTO
                            .builder()
                            .dayOfWeek(workingHour.getDayOfWeek())
                            .startTime(workingHour.getStartTime())
                            .endTime(workingHour.getEndTime())
                            .build()
            );
        });
        return workingHoursDTOS;
    }


    //Returns daily working hours with specified timezone.
    //If timezone is not specified on path it returns as the team member's zone
    @Override
    public List<WorkingHoursDTO> getDailyWorkingHoursZoned(Integer teamId, Integer memberId, String timezone) throws RuntimeException {

        //Get user's timezone from team member
        if(timezone == null)
            timezone = teamMemberRepository.findTimezoneByTeamAndMemberId(teamId, memberId).orElseThrow(()-> new RuntimeException("User is not present"));
        else
        {
            timezone = timezone.replace('_','/');
        }
        if(timezone == null){
            throw new RuntimeException("Timezone is not set");
        }

        ZoneId zoneId = ZoneId.of(timezone);

        //Get Team Member
        TeamMember teamMember = teamMemberRepository.findById(memberId).orElseThrow(()-> new RuntimeException("Team member is not present"));
        List<WorkingHours> workingHours = workingHoursRepository.getWorkingHoursByTeamMember(teamMember).orElseThrow(()-> new RuntimeException("Working Hours is not present"));


        List<WorkingHoursDTO> workingHoursDTOS = new ArrayList<>();
        workingHours.forEach(workingHour -> {
            ZonedDateTime startDateZonedTime = ZonedDateTime.of(workingHour.getStartTime(),ZoneOffset.UTC);
            ZonedDateTime endDateZonedTime = ZonedDateTime.of(workingHour.getEndTime(),ZoneOffset.UTC);

            LocalDateTime startTime = startDateZonedTime.withZoneSameInstant(zoneId).toLocalDateTime();
            LocalDateTime endTime = endDateZonedTime.withZoneSameInstant(zoneId).toLocalDateTime();
            DayOfWeek dayOfWeek = workingHour.getDayOfWeek();
            if(startTime.toLocalDate().isBefore(startDateZonedTime.toLocalDate()))
            {
                dayOfWeek = DayOfWeek.of((workingHour.getDayOfWeek().getValue() - 1) % 7);
            }
            else if (startTime.toLocalDate().isAfter(startDateZonedTime.toLocalDate()))
            {
                dayOfWeek = DayOfWeek.of((workingHour.getDayOfWeek().getValue() + 1) % 7);
            }
            workingHoursDTOS.add(
                    WorkingHoursDTO
                            .builder()
                            .dayOfWeek(dayOfWeek)
                            .startTime(startTime)
                            .endTime(endTime)
                            .build()
            );
        });
        return workingHoursDTOS;
    }

    @Override
    public List<WorkingHoursDTO> getCoreHours(Integer teamId) throws RuntimeException {
        List<WorkingHoursDTO> workingHoursDTOS = new ArrayList<>();
        List<TeamMemberResponse> teamMemberResponses = teamService.getTeamMembers(teamId);

        teamMemberResponses.forEach(teamMemberResponse -> {
            TeamMember teamMember = teamMemberRepository.findById(teamMemberResponse.getTeamMemberId()).orElseThrow(()-> new RuntimeException("Team member is not present"));
            List<WorkingHours> workingHours = workingHoursRepository.getWorkingHoursByTeamMember(teamMember).orElseThrow(()-> new RuntimeException("Working Hours is not present"));


        });

        return workingHoursDTOS;
    }

    @Transactional
    @Override
    //Delete with team member relative Id. Not real ID. So deletes by index.
    public void deleteDailyWorkingHoursById(Integer teamId,Integer memberId,Integer workingHoursId) throws RuntimeException {
        TeamMember teamMember = teamMemberRepository.findById(memberId).orElseThrow(()-> new RuntimeException("Team member is not present"));

        List<WorkingHours> workingHours = workingHoursRepository.getWorkingHoursByTeamMember(teamMember).orElseThrow(()-> new RuntimeException("Working Hours is not present"));
        boolean deleted = false;
        for(int i = 0; i < workingHours.size(); i++){
            WorkingHours workingHour = workingHours.get(i);
            System.out.println(workingHour);
            if(i == workingHoursId)
            {
                workingHoursRepository.delete(workingHour);
                deleted = true;
                break;
            }
        }

        if(!deleted)
            throw new RuntimeException("WorkingHours not found");

    }

}
