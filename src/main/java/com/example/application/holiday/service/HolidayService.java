package com.example.application.holiday.service;

import com.example.application.holiday.model.Holiday;

import java.util.List;

public interface HolidayService {

    void addHoliday(Holiday holiday);

    List<Holiday> getHolidays(String timezone);
}
