package com.example.application.util;

import com.example.application.team.dto.EventResponseDTO;
import com.example.application.team.dto.EventScheduleResponse;
import com.example.application.team.dto.WorkingHoursDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class EventScheduleExcelWriter {

    public static void writeToExcel(EventScheduleResponse response, String filePath) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Event Schedule");

        // DateTime formatter for time
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Create header row
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < 7; i++) {
            headerRow.createCell(i).setCellValue(DayOfWeek.of(i+1).name());
        }
        headerRow.createCell(7).setCellValue(""); // Empty column
        headerRow.createCell(8).setCellValue("Event Details");

        // Fill working hours
        List<WorkingHoursDTO> workingHours = response.getWorkingHours();

        Row[] rows = new Row[7];
        for(int i = 1; i < 8; i++)
        {
            rows[i-1] = sheet.createRow(i);
        }
        for(int i = 1; i < 8; i++)
        {
            int rowIndex = 0;
            for(WorkingHoursDTO workingHoursDTO : workingHours)
            {
                if(workingHoursDTO.getDayOfWeek().getValue() == i)
                {
                    System.out.println(workingHoursDTO.getDayOfWeek() + "-" + workingHoursDTO.getDayOfWeek().getValue());
                    System.out.println("Row: " + (rowIndex) + "  Column: " + (i-1));
                    rows[rowIndex++].createCell(i-1)
                            .setCellValue(workingHoursDTO.getStartTime().format(timeFormatter) + " - " +
                                    workingHoursDTO.getEndTime().format(timeFormatter));
                }
            }
            //rowIndex = 1;
        }
//        Row row = sheet.createRow(1);
//        row.createCell(0).setCellValue(0);
//        row.createCell(1).setCellValue(1);
//
//        row = sheet.createRow(2);
//        row.createCell(0).setCellValue("a");
//        row.createCell(1).setCellValue("b");
//        for (WorkingHoursDTO workingHour : workingHours) {
//            Row row = sheet.createRow(rowIndex++);
//            row.createCell(workingHour.getDayOfWeek().getValue() - 1)
//                    .setCellValue(workingHour.getStartTime().format(timeFormatter) + " - " +
//                            workingHour.getEndTime().format(timeFormatter));
//        }

        // Fill events
        List<EventResponseDTO> events = response.getEvents();
        int rowIndex = 1; // Reset row index for events
        for (EventResponseDTO event : events) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }
            row.createCell(8).setCellValue(
                    "ID: " + event.getEventId() +
                            ", Title: " + event.getTitle() +
                            ", Creator: " + event.getCreatorName() +
                            ", Time: " + event.getStartTime() + " - " +
                            event.getEndTime().format(timeFormatter)
            );
            rowIndex++;
        }

        // Auto-size columns
        for (int i = 0; i < 9; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }
}