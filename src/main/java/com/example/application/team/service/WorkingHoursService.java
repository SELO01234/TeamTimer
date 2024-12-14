package com.example.application.team.service;

import com.example.application.team.dto.CoreHourResponse;
import com.example.application.team.dto.WorkingHoursDTO;

import java.util.List;

public interface WorkingHoursService {

    void setDailyWorkingHours(Integer teamId, Integer memberId, List<WorkingHoursDTO> workingHoursDTO);
    void deleteDailyWorkingHoursById(Integer teamId, Integer memberId,Integer workingHoursId);

    List<WorkingHoursDTO> getDailyWorkingHours(Integer teamId, Integer memberId);
    List<WorkingHoursDTO> getDailyWorkingHoursZoned(Integer teamId, Integer memberId, String timezone);
    List<CoreHourResponse> getCoreHours(Integer teamId);
}
