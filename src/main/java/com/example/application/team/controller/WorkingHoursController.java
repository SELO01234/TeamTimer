package com.example.application.team.controller;

import com.example.application.team.dto.*;
import com.example.application.team.service.WorkingHoursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
public class WorkingHoursController {

    private final WorkingHoursService workingHoursService;
    private final HandlerMapping resourceHandlerMapping;

    @Autowired
    public WorkingHoursController(WorkingHoursService workingHoursService, HandlerMapping resourceHandlerMapping){
        this.workingHoursService = workingHoursService;
        this.resourceHandlerMapping = resourceHandlerMapping;
    }

    @PutMapping("/{teamId}/members/{memberId}/working-hours")
    ResponseEntity<String> setDailyWorkingHours(@PathVariable("teamId") Integer teamId,
                                                @PathVariable("memberId") Integer memberId,
                                                @RequestBody List<WorkingHoursDTO> workingHoursDTOS){
        workingHoursService.setDailyWorkingHours(teamId, memberId, workingHoursDTOS);
        return ResponseEntity.ok("Working hours saved successfully!");
    }

    @GetMapping("/{teamId}/members/{memberId}/working-hours")
    ResponseEntity<List<WorkingHoursDTO>> getDailyWorkingHours(@PathVariable("teamId") Integer teamId,
                                                               @PathVariable("memberId") Integer memberId){
        return ResponseEntity.ok().body(workingHoursService.getDailyWorkingHours(teamId,memberId));
    }

    @GetMapping("/{teamId}/members/{memberId}/working-hours-zoned")
    ResponseEntity<List<WorkingHoursDTO>> getDailyWorkingHoursZoned(@PathVariable("teamId") Integer teamId,
                                                                    @PathVariable("memberId") Integer memberId,
                                                                    @RequestParam(required = false) String timezone){
        return ResponseEntity.ok().body(workingHoursService.getDailyWorkingHoursZoned(teamId,memberId,timezone));
    }

    @DeleteMapping("/{teamId}/members/{memberId}/working-hours/{workingHoursId}")
    ResponseEntity<String> deleteDailyWorkingHours(@PathVariable("teamId") Integer teamId,
                                                  @PathVariable("memberId") Integer memberId,
                                                  @PathVariable("workingHoursId") Integer workingHoursId){
        workingHoursService.deleteDailyWorkingHoursById(teamId, memberId, workingHoursId);
        return ResponseEntity.ok("Working hours deleted successfully!");
    }

    @GetMapping("/{teamId}/core-hours")
    ResponseEntity<List<WorkingHoursDTO>> getCoreHours(@PathVariable("teamId") Integer teamId,
                                                       @RequestParam(required = false) String timezone){
        return ResponseEntity.ok().body(workingHoursService.getOverlapHours(teamId, timezone, new ArrayList<>()));
    }

    @PostMapping("/{teamId}/overlap-hours")
    ResponseEntity<List<WorkingHoursDTO>> getOverlapHours(@PathVariable("teamId") Integer teamId,
                                                          @RequestParam(required = false) String timezone,
                                                          @RequestBody List<Integer> teamMemberIds){
        return ResponseEntity.ok().body(workingHoursService.getOverlapHours(teamId, timezone, teamMemberIds));
    }

    @GetMapping("/{teamId}/team-availability")
    ResponseEntity<List<List<WorkingHoursDTO>>> getTeamAvailability(@PathVariable("teamId") Integer teamId,
                                                                    @RequestParam(required = false) String timezone,
                                                                    @RequestParam(required = false) Integer minMemberCount){
        return ResponseEntity.ok().body(workingHoursService.getTeamAvailability(teamId, timezone,minMemberCount));
    }

    @PostMapping("/{teamId}/members/{memberId}/time-off")
    ResponseEntity<String> setTimeOffRequest(@PathVariable("teamId") Integer teamId,
                                             @PathVariable("memberId") Integer memberId,
                                             @RequestBody List<TimeOffRequestDTO> workingHoursDTOS){

        workingHoursService.setTimeOffRequest(teamId,memberId,workingHoursDTOS);
        return ResponseEntity.ok("Time off request sent!");
    }

    @GetMapping("/{teamId}/members/{memberId}/time-off")
    ResponseEntity<List<TimeOffRequestDTO>> getTimeOffRequest(@PathVariable("teamId") Integer teamId,
                                                              @PathVariable("memberId") Integer memberId,
                                                              @RequestParam(required = false) String timezone){

        return ResponseEntity.ok().body(workingHoursService.getTimeOffRequest(teamId,memberId,timezone));
    }

    @PostMapping("/{teamId}/events-add")
    ResponseEntity<String> scheduleMeeting(@PathVariable("teamId") Integer teamId, @RequestBody EventRegisterDTO eventRegisterDTO){
        return ResponseEntity.ok(workingHoursService.scheduleMeeting(eventRegisterDTO, teamId));
    }

    @GetMapping("/{teamId}/events")
    ResponseEntity<List<EventResponseDTO>> getTeamsEvents(@PathVariable("teamId") Integer teamId){
        return ResponseEntity.ok(workingHoursService.getTeamsEvents(teamId));
    }

    @GetMapping("/{teamId}/members/{memberId}/schedule")
    ResponseEntity<EventScheduleResponse> getTeamMemberSchedule(@PathVariable("teamId") Integer teamId,
                                                                @RequestParam(required = false) String timezone,
                                                                @PathVariable("memberId") Integer memberId){
        return ResponseEntity.ok().body(workingHoursService.getTeamMemberSchedule(teamId, timezone, memberId));
    }

    @GetMapping("/{teamId}/members/{memberId}/schedule-excel")
    ResponseEntity<String> getScheduleAsExcel(@PathVariable("teamId") Integer teamId,
                                            @PathVariable("memberId") Integer memberId,
                                              @RequestParam(required = false) String timezone) throws IOException, IllegalAccessException {
        workingHoursService.getScheduleAsExcel(teamId,memberId,timezone);
        return ResponseEntity.ok().body("Excel exported!");
    }

}
