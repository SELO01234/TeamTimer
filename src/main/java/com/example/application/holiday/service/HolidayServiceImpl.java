package com.example.application.holiday.service;

import com.example.application.holiday.model.Holiday;
import com.example.application.holiday.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HolidayServiceImpl implements HolidayService{

    private final HolidayRepository holidayRepository;

    @Autowired
    public HolidayServiceImpl(HolidayRepository holidayRepository){
        this.holidayRepository = holidayRepository;
    }

    @Override
    public void addHoliday(Holiday holiday) throws RuntimeException{
        //check if holiday exists
        if(holidayRepository.existsByAllAttributes(holiday.getTimezone(), holiday.getHolidayName(), holiday.getDate())){
            throw new RuntimeException("Holiday is already saved");
        }

        //save holiday
        holidayRepository.save(holiday);
    }

    @Override
    public List<Holiday> getHolidays(String timezone) throws RuntimeException {
        return holidayRepository.findByRegionCode(timezone).orElseThrow(()-> new RuntimeException("Could not retrieve holidays"));
    }
}
