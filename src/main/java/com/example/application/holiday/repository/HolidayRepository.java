package com.example.application.holiday.repository;

import com.example.application.holiday.Entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HolidayRepository extends JpaRepository<Integer, Holiday> {
}
