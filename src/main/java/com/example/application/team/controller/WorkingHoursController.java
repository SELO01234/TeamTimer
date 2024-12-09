package com.example.application.team.controller;

import com.example.application.team.dto.WorkingHoursRequest;
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
                                                @RequestBody List<WorkingHoursRequest> workingHoursRequests){
        workingHoursService.setDailyWorkingHours(teamId, memberId, workingHoursRequests);
        return ResponseEntity.ok("Working hours saved successfully!");
    }
}
