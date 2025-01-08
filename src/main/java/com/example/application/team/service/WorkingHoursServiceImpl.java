package com.example.application.team.service;

import com.example.application.holiday.model.Holiday;
import com.example.application.holiday.repository.HolidayRepository;
import com.example.application.team.calculationmodels.CalcEvent;
import com.example.application.team.calculationmodels.CalcEventType;
import com.example.application.team.dto.*;
import com.example.application.team.model.*;
import com.example.application.team.repository.*;
import com.example.application.util.EventScheduleExcelWriter;
import com.example.application.util.TimeConverter;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class WorkingHoursServiceImpl implements WorkingHoursService{

    private final WorkingHoursRepository workingHoursRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TimeOffRequestRepository timeOffRequestRepository;
    private final EventRepository eventRepository;
    private final TeamRepository teamRepository;
    private final HolidayRepository holidayRepository;

    @Autowired
    public WorkingHoursServiceImpl(WorkingHoursRepository workingHoursRepository,
                                   TeamMemberRepository teamMemberRepository,
                                   TimeOffRequestRepository timeOffRequestRepository,
                                   EventRepository eventRepository,
                                   TeamRepository teamRepository,
                                   HolidayRepository holidayRepository) {
        this.workingHoursRepository = workingHoursRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.timeOffRequestRepository = timeOffRequestRepository;
        this.eventRepository = eventRepository;
        this.teamRepository = teamRepository;
        this.holidayRepository = holidayRepository;
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
                    if(teamMemberIds.contains(workingHour.getTeamMember().getTeamMemberId()))
                    {
                        tempTeamMemberIds.add(workingHour.getTeamMember().getTeamMemberId());
                    }
                }

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

                    List<TimeInterval> coreIntervals = findMaxOverlapIntervals(refinedWorkingHourList, teamMemberIds.size());

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

        //check holidays
        List<Holiday> holidays = holidayRepository.findAll();

        List<WorkingHoursDTO> convertedHolidays = holidays.stream().map((holiday) -> {
            LocalDateTime holidayStartTime = holiday.getDate().atStartOfDay();
            LocalDateTime holidayEndTime = holiday.getDate().atTime(LocalTime.of(23, 59, 0));
            return TimeConverter.convertZonedTimeToUtcTime(holidayStartTime, holidayEndTime, holiday.getTimezone(), DayOfWeek.MONDAY);
        }).toList();

        coreHours = coreHours.stream().filter((coreHour) -> {
            for (WorkingHoursDTO holiday : convertedHolidays) {
                // If coreHour overlaps with holiday, exclude it
                if ((holiday.getStartTime().isBefore(coreHour.getEndTime()) || holiday.getStartTime().isEqual(coreHour.getEndTime()))
                        && (holiday.getEndTime().isAfter(coreHour.getStartTime()) || holiday.getEndTime().isEqual(coreHour.getStartTime()))) {
                    return false; // Exclude this coreHour
                }
            }
            return true; // Keep this coreHour
        }).toList(); // Convert the stream back to a list (Java 16+). Use `collect(Collectors.toList())` for earlier versions.

        //check timeoff request for users
        List<TimeOffRequest> timeOffRequestList = timeOffRequestRepository.findAllByApproved(true);

        coreHours = coreHours.stream().filter((coreHour) -> {
            for (TimeOffRequest timeOffRequest : timeOffRequestList) {
                // If coreHour overlaps with holiday, exclude it
                if ((timeOffRequest.getStartDate().isBefore(coreHour.getEndTime()) || timeOffRequest.getStartDate().isEqual(coreHour.getEndTime()))
                        && (timeOffRequest.getEndDate().isAfter(coreHour.getStartTime()) || timeOffRequest.getEndDate().isEqual(coreHour.getStartTime()))) {
                    return false; // Exclude this coreHour
                }
            }
            return true; // Keep this coreHour
        }).toList(); // Convert the stream back to a list (Java 16+). Use `collect(Collectors.toList())` for earlier versions.

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

    @Override
    public List<List<WorkingHoursDTO>> getTeamAvailability(Integer teamId, String timezone,Integer minMemberCount) {
        List<List<WorkingHoursDTO>> teamAvailability = new ArrayList<>();
        // Traverse over all days of the week
        for (DayOfWeek day : DayOfWeek.values()) {
            // Retrieve working hours for the given team and day
            int teammemberCount = teamMemberRepository.findAllByTeamId(teamId).size();
            List<WorkingHours> workingHourList = workingHoursRepository.getWorkingHoursByTeamIdAndDay(teamId, day.name());
            if(workingHourList.isEmpty())
                continue;

            if(minMemberCount == null) minMemberCount = 1;
            else if(minMemberCount > teammemberCount) minMemberCount = teammemberCount;

            if(timezone == null) timezone = "UTC";

            List<TimeInterval> coreHours = findMaxOverlapIntervals(workingHourList,minMemberCount);

            List<WorkingHoursDTO> workingHoursDTOS = new ArrayList<>();
            for (TimeInterval interval : coreHours) {
                workingHoursDTOS.add(TimeConverter.convertTimeToZonedTime(
                        interval.getStart(),
                        interval.getEnd(),
                        timezone,
                        day));
            }
            teamAvailability.add(workingHoursDTOS);
        }

        return teamAvailability;
    }


    private List<TimeInterval> findMaxOverlapIntervals(List<WorkingHours> workingHours, Integer numberOfTeamMembers) {
        List<CalcEvent> events = new ArrayList<>();

        // Convert each interval to a pair of events (start and end)
        for (WorkingHours wh : workingHours) {
            events.add(new CalcEvent(wh.getStartTime(), CalcEventType.START));
            events.add(new CalcEvent(wh.getEndTime(), CalcEventType.END));
        }

        // Sort events by time (start events come before end events if times are equal)
        Comparator<CalcEvent> comparator = Comparator
                .comparing((CalcEvent event) -> event.getTime().toLocalTime())
                .thenComparing(CalcEvent::getType);

        events.sort(comparator);
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
                if (activeCount == maxCount && maxCount>=numberOfTeamMembers && currentStart != null) {
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

    @Transactional
    @Override
    public void setTimeOffRequest(Integer teamId, Integer memberId, List<TimeOffRequestDTO> timeOffRequests) throws RuntimeException {

        TeamMember member = teamMemberRepository.findById(memberId).orElseThrow(()-> new RuntimeException("Team member is not present"));

        List<TimeOffRequestDTO> zonedRequests = new ArrayList<>();
        for (TimeOffRequestDTO timeOffRequest : timeOffRequests){
            zonedRequests.add(TimeConverter.convertZonedTimeToUtcTimeTimeOffRequest(timeOffRequest,member.getUser().getTimezone()));
        }
        for (TimeOffRequestDTO timeOffRequest : zonedRequests) {
            timeOffRequestRepository.save(TimeOffRequest
                                        .builder()
                    .teamMember(member)
                    .startDate(timeOffRequest.getStartDate())
                    .endDate(timeOffRequest.getEndDate())
                    .reason(timeOffRequest.getReason())
                    .approved(timeOffRequest.isApproved())
                    .build());
        }
    }

    @Override
    public List<TimeOffRequestDTO> getTimeOffRequest(Integer teamId, Integer memberId,String timezone) throws RuntimeException {
        TeamMember member = teamMemberRepository.findById(memberId).orElseThrow(()-> new RuntimeException("Team member is not present"));
        if(timezone == null)
            timezone = "UTC";

        List<TimeOffRequest> timeOffRequestList = timeOffRequestRepository.getAllByTeamMember(member);
        List<TimeOffRequestDTO> timeOffRequestDTOS = new ArrayList<>();

        for (TimeOffRequest timeOffRequest : timeOffRequestList) {
            timeOffRequestDTOS.add(
                    TimeOffRequestDTO
                            .builder()
                            .startDate(timeOffRequest.getStartDate())
                            .endDate(timeOffRequest.getEndDate())
                            .reason(timeOffRequest.getReason())
                            .approved(timeOffRequest.isApproved())
                            .build()
                    
            );
        }

        List<TimeOffRequestDTO> zonedRequests = new ArrayList<>();
        for (TimeOffRequestDTO timeOffRequest : timeOffRequestDTOS){
            zonedRequests.add(TimeConverter.convertTimeToZonedTimeTimeOffRequest(timeOffRequest,timezone));
        }

        return zonedRequests;
    }

    @Override
    public String scheduleMeeting(EventRegisterDTO eventRegisterDTO, Integer teamId) {
        //find team member
        TeamMember teamMember = teamMemberRepository
                .findById(eventRegisterDTO.getCreatorId())
                .orElseThrow(()-> new RuntimeException("User is not present"));

        //find team
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team is not present"));

        //convert start and end time to utc-0
        WorkingHoursDTO convertedTimeResponse = TimeConverter.convertZonedTimeToUtcTime(eventRegisterDTO.getStartTime(), eventRegisterDTO.getEndTime(), eventRegisterDTO.getTimezone(), eventRegisterDTO.getDayOfWeek());

        //get overlapped hours for users
        List<WorkingHoursDTO> overlappedHours = getOverlapHours(teamId,null,eventRegisterDTO.getTeamMemberIds());

        if(overlappedHours.size() == 0 ){
            return "These users do not have overlapped hours";
        }

        //filter overlapped hours to compare the focused day
        List<WorkingHoursDTO> filteredOverlappedHours = overlappedHours.stream()
                .filter(overlappedHour -> overlappedHour.getDayOfWeek() == eventRegisterDTO.getDayOfWeek())
                .toList();

        if(filteredOverlappedHours.size() == 0 ){
            return "These users do not have overlapped hours on: " + eventRegisterDTO.getDayOfWeek().name();
        }

        //compare overlapped hours with event time
        AtomicBoolean suitable= new AtomicBoolean(false);

        filteredOverlappedHours.forEach((overlappedHour) -> {
            if((overlappedHour.getStartTime().isBefore(convertedTimeResponse.getStartTime()) || overlappedHour.getStartTime().isEqual(convertedTimeResponse.getStartTime()))
                    && (overlappedHour.getEndTime().isAfter(convertedTimeResponse.getEndTime()) || overlappedHour.getEndTime().isEqual(convertedTimeResponse.getEndTime()))
            ){
                suitable.set(true);
            }
        });

        if(suitable.get()){
            //save event
            eventRepository.save(
                    Event
                            .builder()
                            .createdBy(teamMember)
                            .title(eventRegisterDTO.getTitle())
                            .team(team)
                            .dayOfWeek(eventRegisterDTO.getDayOfWeek())
                            .startTime(convertedTimeResponse.getStartTime())
                            .endTime(convertedTimeResponse.getEndTime())
                            .build()
            );

            return "Event is successfully saved";
        }
        else{
            return "Event is not suitable overlapped hours for " + eventRegisterDTO.getDayOfWeek().name() + " is: " + filteredOverlappedHours;
        }
    }

    @Override
    public List<EventResponseDTO> getTeamsEvents(Integer teamId) {
        //find the team
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new RuntimeException("Team is not present"));
        List<Event> events = eventRepository.findByTeam(team).orElseThrow(()-> new RuntimeException("Cannot get teams events"));

        List<EventResponseDTO> eventResponseDTOS = events.stream().map((event) -> {
            return EventResponseDTO
                    .builder()
                    .eventId(event.getEventId())
                    .title(event.getTitle())
                    .creatorId(event.getCreatedBy().getTeamMemberId())
                    .creatorName(event.getCreatedBy().getUser().getUsername())
                    .dayOfWeek(event.getDayOfWeek())
                    .startTime(event.getStartTime())
                    .endTime(event.getEndTime())
                    .build();
        }).toList();

        return eventResponseDTOS;
    }

    @Override
    public EventScheduleResponse getTeamMemberSchedule(Integer teamId, String timezone, Integer memberId) {
        TeamMember teamMember = teamMemberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("Team member is not present"));
        List<WorkingHours> workingHours = workingHoursRepository.getWorkingHoursByTeamMember(teamMember).orElseThrow(()-> new RuntimeException("Cannot retrieve hours"));
        Team team = teamRepository.findById(teamId).orElseThrow(()-> new RuntimeException("Cannot retrieve team"));
        List<Event> events = eventRepository.findByTeam(team).orElseThrow(()-> new RuntimeException("Cannot retrieve hours"));

        List<WorkingHoursDTO> workingHoursDTOS = workingHours.stream().map((workingHour) -> {
            if(timezone != null){
                return TimeConverter.convertTimeToZonedTime(workingHour.getStartTime(), workingHour.getEndTime(), timezone, workingHour.getDayOfWeek());
            }
            else{
                return WorkingHoursDTO
                        .builder()
                        .startTime(workingHour.getStartTime())
                        .endTime(workingHour.getEndTime())
                        .dayOfWeek(workingHour.getDayOfWeek())
                        .build();
            }
        }).toList();

        if(events != null){
            events = events.stream().filter((event -> {
                boolean suitable=false;
                for (WorkingHoursDTO workingHour: workingHoursDTOS) {
                    if(
                            (workingHour.getStartTime().isEqual(event.getStartTime()) || workingHour.getStartTime().isBefore(event.getStartTime()))
                            &&
                            (workingHour.getEndTime().isEqual(event.getEndTime()) || workingHour.getEndTime().isAfter(event.getEndTime()))
                    ){
                        suitable=true;
                    }
                }
                return suitable;
            })).toList();
        }

        List<EventResponseDTO> eventResponseDTOS = null;

        if(timezone != null){
            eventResponseDTOS = events.stream().map((event) -> {
                WorkingHoursDTO workingHoursDTO = TimeConverter.convertTimeToZonedTime(event.getStartTime(), event.getEndTime(), timezone, event.getDayOfWeek());
                return EventResponseDTO
                        .builder()
                        .eventId(event.getEventId())
                        .title(event.getTitle())
                        .creatorId(event.getCreatedBy().getTeamMemberId())
                        .creatorName(event.getCreatedBy().getUser().getUsername())
                        .dayOfWeek(workingHoursDTO.getDayOfWeek())
                        .startTime(workingHoursDTO.getStartTime())
                        .endTime(workingHoursDTO.getEndTime())
                        .build();
            }).toList();
        }
        else{
            eventResponseDTOS = events.stream().map((event) -> {
                return EventResponseDTO
                        .builder()
                        .eventId(event.getEventId())
                        .title(event.getTitle())
                        .creatorId(event.getCreatedBy().getTeamMemberId())
                        .creatorName(event.getCreatedBy().getUser().getUsername())
                        .dayOfWeek(event.getDayOfWeek())
                        .startTime(event.getStartTime())
                        .endTime(event.getEndTime())
                        .build();
            }).toList();
        }

        return EventScheduleResponse
                .builder()
                .workingHours(workingHoursDTOS)
                .events(eventResponseDTOS)
                .build();
    }

    @Override
    public void getScheduleAsExcel(Integer teamId, Integer memberId, String timezone) throws RuntimeException, IOException, IllegalAccessException {

        String path="/excels";

        TeamMember member = teamMemberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("Member is not present"));

        EventScheduleResponse schedule = getTeamMemberSchedule(teamId,timezone,memberId);
        if(schedule == null){
            throw new RuntimeException("Schedule is not present");
        }
        String fileName = path + "/" + member.getUser().getUsername() + "_Schedule.xlsx";

        EventScheduleExcelWriter.writeToExcel(schedule,fileName);
    }

}
