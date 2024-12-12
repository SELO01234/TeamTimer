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
import java.util.ArrayList;
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

    @Transactional
    @Override
    public List<WorkingHoursRequest> getDailyWorkingHours(Integer teamId, Integer memberId) throws RuntimeException {

        //Get Team Member
        TeamMember teamMember = teamMemberRepository.findById(memberId).orElseThrow(()-> new RuntimeException("Team member is not present"));
        List<WorkingHours> workingHours = workingHoursRepository.getWorkingHoursByTeamMember(teamMember);

        if(workingHours.isEmpty()){
            return new ArrayList<>();
        }
        List<WorkingHoursRequest> workingHoursRequests = new ArrayList<>();
        workingHours.forEach(workingHour -> {
            System.out.println(workingHour.getStartTime());
            workingHoursRequests.add(
                    WorkingHoursRequest
                            .builder()
                            .dayOfWeek(workingHour.getDayOfWeek())
                            .startTime(workingHour.getStartTime())
                            .endTime(workingHour.getEndTime())
                            .build()
            );
        });
        return workingHoursRequests;
    }


    //Returns daily working hours with specified timezone.
    //If timezone is not specified on path it returns as the team member's zone
    @Transactional
    @Override
    public List<WorkingHoursRequest> getDailyWorkingHoursZoned(Integer teamId, Integer memberId,String timezone) throws RuntimeException {

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
        List<WorkingHours> workingHours = workingHoursRepository.getWorkingHoursByTeamMember(teamMember);

        if(workingHours.isEmpty()){
            return new ArrayList<>();
        }

        List<WorkingHoursRequest> workingHoursRequests = new ArrayList<>();
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
            workingHoursRequests.add(
                    WorkingHoursRequest
                            .builder()
                            .dayOfWeek(dayOfWeek)
                            .startTime(startTime)
                            .endTime(endTime)
                            .build()
            );
        });
        return workingHoursRequests;
    }

    @Transactional
    @Override
    //Delete with team member relative Id. Not real ID. So deletes by index.
    public void deleteDailyWorkingHoursById(Integer teamId,Integer memberId,Integer workingHoursId) throws RuntimeException {
        TeamMember teamMember = teamMemberRepository.findById(memberId).orElseThrow(()-> new RuntimeException("Team member is not present"));

        List<WorkingHours> workingHours = workingHoursRepository.getWorkingHoursByTeamMember(teamMember);
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
