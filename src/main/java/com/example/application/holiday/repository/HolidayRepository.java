package com.example.application.holiday.repository;

import com.example.application.holiday.model.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Integer> {

    @Query(value = "SELECT EXISTS(SELECT * FROM holiday WHERE timezone=:timezone AND holiday_name=:holiday_name AND date=:date)", nativeQuery = true)
    boolean existsByAllAttributes(@Param("timezone") String timezone,@Param("holiday_name") String holidayName,@Param("date") LocalDate date);

    @Query(value = "SELECT * FROM holiday WHERE timezone=:timezone", nativeQuery = true)
    Optional<List<Holiday>> findByRegionCode(@Param("timezone") String timezone);
}
