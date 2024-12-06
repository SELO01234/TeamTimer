package com.example.application.holiday.service;

import com.example.application.holiday.Entity.Holiday;
import com.example.application.holiday.repository.HolidayRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HolidayServiceImpl implements HolidayService{

    private final HolidayRepository holidayRepository;

    @Autowired
    public HolidayServiceImpl(HolidayRepository holidayRepository){
        this.holidayRepository = holidayRepository;
    }

    @Override
    public void addHoliday(Holiday holiday) throws RuntimeException{
        // TODO
    }
}
