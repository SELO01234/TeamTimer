package com.example.application.team.service;

import com.example.application.team.dto.*;

import java.util.List;

public interface WorkingHoursService {

    void setDailyWorkingHours(Integer teamId, Integer memberId, List<WorkingHoursDTO> workingHoursDTO);
    void deleteDailyWorkingHoursById(Integer teamId, Integer memberId,Integer workingHoursId);
    void setTimeOffRequest(Integer teamId, Integer memberId, List<TimeOffRequestDTO> timeOffRequests);

    List<WorkingHoursDTO> getDailyWorkingHours(Integer teamId, Integer memberId);
    List<WorkingHoursDTO> getDailyWorkingHoursZoned(Integer teamId, Integer memberId, String timezone);

    List<WorkingHoursDTO> getOverlapHours(Integer teamId, String timezone, List<Integer> teamMemberIds);
    List<List<WorkingHoursDTO>> getTeamAvailability(Integer teamId, String timezone,Integer minMemberCount);

    List<TimeOffRequestDTO> getTimeOffRequest(Integer teamId, Integer memberId);

    String scheduleMeeting(EventRegisterDTO eventRegisterDTO, Integer teamId);

    List<EventResponseDTO> getTeamsEvents(Integer teamId);

    EventScheduleResponse getTeamMemberSchedule(Integer teamId, String timezone, Integer memberId);
}
