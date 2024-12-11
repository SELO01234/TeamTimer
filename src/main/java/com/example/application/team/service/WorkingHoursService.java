package com.example.application.team.service;

import com.example.application.team.dto.WorkingHoursRequest;

import java.util.List;

public interface WorkingHoursService {

    void setDailyWorkingHours(Integer teamId, Integer memberId, List<WorkingHoursRequest> workingHoursRequest);
    void deleteDailyWorkingHoursById(Integer teamId, Integer memberId,Integer workingHoursId);

    List<WorkingHoursRequest> getDailyWorkingHours(Integer teamId, Integer memberId);
    List<WorkingHoursRequest> getDailyWorkingHoursZoned(Integer teamId, Integer memberId,String timezone);
}
