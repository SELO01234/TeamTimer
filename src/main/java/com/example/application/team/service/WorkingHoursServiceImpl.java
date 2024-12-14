package com.example.application.team.service;

import com.example.application.team.calculationmodels.CalcEvent;
import com.example.application.team.calculationmodels.CalcEventType;
import com.example.application.team.dto.TimeInterval;
import com.example.application.team.dto.WorkingHoursDTO;
import com.example.application.team.model.TeamMember;
import com.example.application.team.model.WorkingHours;
import com.example.application.team.repository.TeamMemberRepository;
import com.example.application.team.repository.WorkingHoursRepository;
import com.example.application.util.TimeConverter;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class WorkingHoursServiceImpl implements WorkingHoursService{

    private final WorkingHoursRepository workingHoursRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Autowired
    public WorkingHoursServiceImpl(WorkingHoursRepository workingHoursRepository,
                                   TeamMemberRepository teamMemberRepository) {
        this.workingHoursRepository = workingHoursRepository;
        this.teamMemberRepository = teamMemberRepository;
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
            //if start time is 22.00 and end time is 04.00 so its next day
            if(utcStartTime.toLocalTime().isAfter(utcEndTime.toLocalTime())){
                // 17.00 - 04.00  --> 17.00 - 23.59 + 00.00 - 04.00
                LocalDateTime StartTime2 = LocalDateTime.of(startDateZonedTime.toLocalDate().plusDays(1), LocalTime.of(0,0));
                LocalDateTime EndTime1 = LocalDateTime.of(startDateZonedTime.toLocalDate(), LocalTime.of(23,59));
                //First interval
                workingHoursRepository.save(
                        WorkingHours
                                .builder()
                                .dayOfWeek(request.getDayOfWeek())
                                .startTime(utcStartTime)
                                .endTime(EndTime1)
                                .teamMember(teamMember)
                                .build()
                );
                //Second interval
                workingHoursRepository.save(
                        WorkingHours
                                .builder()
                                .dayOfWeek(request.getDayOfWeek().plus(1))
                                .startTime(StartTime2)
                                .endTime(utcEndTime.plusDays(1))
                                .teamMember(teamMember)
                                .build()
                );
            }
            else
            {
                workingHoursRepository.save(
                        WorkingHours
                                .builder()
                                .dayOfWeek(request.getDayOfWeek())
                                .startTime(utcStartTime)
                                .endTime(utcEndTime)
                                .teamMember(teamMember)
                                .build()
                );
            }

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

        //check if timezone is assigned
        if(timezone == null){
            timezone = teamMemberRepository.findTimezoneByTeamAndMemberId(teamId, memberId).orElseThrow(()-> new RuntimeException("User is not present"));
        }

        //Get Team Member
        TeamMember teamMember = teamMemberRepository.findById(memberId).orElseThrow(()-> new RuntimeException("Team member is not present"));
        List<WorkingHours> workingHours = workingHoursRepository.getWorkingHoursByTeamMember(teamMember).orElseThrow(()-> new RuntimeException("Working Hours is not present"));


        List<WorkingHoursDTO> workingHoursDTOS = new ArrayList<>();

        String finalTimezone = timezone;
        workingHours.forEach(workingHour -> {
            workingHoursDTOS.add(
                    TimeConverter.convertTimeToZonedTime(workingHour.getStartTime(), workingHour.getEndTime(), finalTimezone, workingHour.getDayOfWeek())
            );
        });
        return workingHoursDTOS;
    }



    @Override
    public List<WorkingHoursDTO> getOverlapHours(Integer teamId, String timezone, List<Integer> teamMemberIds) {
        List<WorkingHoursDTO> coreHours = new ArrayList<>();
        // Traverse over all days of the week
        for (DayOfWeek day : DayOfWeek.values()) {
            // Retrieve working hours for the given team and day
            List<WorkingHours> workingHourList = workingHoursRepository.getWorkingHoursByTeamIdAndDay(teamId, day.name());

            if(teamMemberIds.isEmpty())
                teamMemberIds = workingHoursRepository.getTeamMemberIds();
            // Process only if there are working hours
            if (!workingHourList.isEmpty()) {

                List<Integer> tempTeamMemberIds = new ArrayList<>();
                // check whether that day has more than one user
                for(WorkingHours workingHour : workingHourList){
                    if(!tempTeamMemberIds.contains(workingHour.getTeamMember().getTeamMemberId()))
                    {
                        tempTeamMemberIds.add(workingHour.getTeamMember().getTeamMemberId());
                    }
                }
                Collections.sort(tempTeamMemberIds);
                Collections.sort(tempTeamMemberIds);
                boolean hasRequiredMembers = tempTeamMemberIds.containsAll(teamMemberIds);

                if(hasRequiredMembers) {
                    //Refine the list for the specified team members
                    List<WorkingHours> refinedWorkingHourList = new ArrayList<>();

                    if (!teamMemberIds.isEmpty()) {
                        for (WorkingHours workingHours : workingHourList) {
                            if (teamMemberIds.contains(workingHours.getTeamMember().getTeamMemberId())) {
                                refinedWorkingHourList.add(workingHours);
                            }
                        }
                    } else {
                        refinedWorkingHourList = workingHourList;
                    }

                    List<TimeInterval> coreIntervals = findMaxOverlapIntervals(refinedWorkingHourList);

                    // Add core intervals for the day
                    for (TimeInterval interval : coreIntervals) {
                        coreHours.add(
                                WorkingHoursDTO
                                        .builder()
                                        .dayOfWeek(day)
                                        .startTime(interval.getStart())
                                        .endTime(interval.getEnd())
                                        .build()
                        );
                    }
                }
            }
        }

        if(timezone == null){
            return coreHours;
        }
        else{
            List<WorkingHoursDTO> zonedWorkingHours = new ArrayList<>();
            coreHours.forEach(coreHour ->{
                zonedWorkingHours.add(
                        TimeConverter.convertTimeToZonedTime(coreHour.getStartTime(), coreHour.getEndTime(), timezone, coreHour.getDayOfWeek())
                );
            });

            return zonedWorkingHours;
        }
    }

    private List<TimeInterval> findMaxOverlapIntervals(List<WorkingHours> workingHours) {
        List<CalcEvent> events = new ArrayList<>();

        // Convert each interval to a pair of events (start and end)
        for (WorkingHours wh : workingHours) {
            events.add(new CalcEvent(wh.getStartTime(), CalcEventType.START));
            events.add(new CalcEvent(wh.getEndTime(), CalcEventType.END));
        }

        // Sort events by time (start events come before end events if times are equal)
        events.sort(Comparator.comparing(CalcEvent::getTime).thenComparing(CalcEvent::getType));
        System.out.println(events);
        int activeCount = 0, maxCount = 0;
        LocalDateTime currentStart = null;
        List<TimeInterval> coreIntervals = new ArrayList<>();

        // Sweep through events to find intervals with maximum overlap
        for (CalcEvent event : events) {
            if (event.getType() == CalcEventType.START) {
                activeCount++;
                if (activeCount > maxCount) {
                    maxCount = activeCount;
                    currentStart = event.getTime(); // Start of a new max interval
                    coreIntervals.clear(); // Reset intervals, new maximum found
                }

                if (activeCount == maxCount && currentStart == null) {
                    currentStart = event.getTime(); // Continue interval if already at max
                }
            } else {
                if (activeCount == maxCount && currentStart != null) {
                    coreIntervals.add(new TimeInterval(currentStart, event.getTime()));
                    currentStart = null; // Reset current start
                }
                activeCount--;
            }
        }

        return coreIntervals;
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
