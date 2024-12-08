package com.example.application.holiday.controller;

import com.example.application.holiday.model.Holiday;
import com.example.application.holiday.service.HolidayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/holidays")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService){
        this.holidayService = holidayService;
    }

    @PostMapping("/add-holiday")
    public ResponseEntity<String> addHoliday(@RequestBody Holiday holiday){
        holidayService.addHoliday(holiday);
        return ResponseEntity.ok().body("Holiday has added");
    }

    @GetMapping("/{regionCode}")
    public ResponseEntity<List<Holiday>> getHolidays(@PathVariable("regionCode") String regionCode){
        return ResponseEntity.ok().body(holidayService.getHolidays(regionCode));
    }
}
