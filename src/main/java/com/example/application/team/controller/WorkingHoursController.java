package com.example.application.team.controller;

import com.example.application.team.dto.WorkingHoursDTO;
import com.example.application.team.service.WorkingHoursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
public class WorkingHoursController {

    private final WorkingHoursService workingHoursService;

    @Autowired
    public WorkingHoursController(WorkingHoursService workingHoursService){
        this.workingHoursService = workingHoursService;
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
    ResponseEntity<List<WorkingHoursDTO>> getCoreHours(@PathVariable("teamId") Integer teamId){
        return ResponseEntity.ok().body(workingHoursService.getCoreHours(teamId));
    }
}
