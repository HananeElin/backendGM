package com.example.ExcelReader.Controller;

import com.example.ExcelReader.Entity.Download_logs;
import com.example.ExcelReader.Service.Download_logs_service;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

public class Download_logs_Controller {


    private Download_logs_service downloadLogsservice;

    // Endpoint for searchinh by fil Name
    @GetMapping("/search-by-filename")
    public ResponseEntity<List<Download_logs>> searchByFileName(@RequestParam String fileName) {
        List<Download_logs> results = downloadLogsservice.searchByFileName(fileName);
        return ResponseEntity.ok(results);
    }

    // Endpoint for searching with an exact date
    @GetMapping("/search-by-date")
    public ResponseEntity<List<Download_logs>> searchByDownloadDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime downloadDate) {
        List<Download_logs> results = downloadLogsservice.searchByDownloadDate(downloadDate);
        return ResponseEntity.ok(results);
    }

    // Endpoint for searching between two dates
    @GetMapping("/search-by-date-range")
    public ResponseEntity<List<Download_logs>> searchByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Download_logs> results = downloadLogsservice.searchByDateRange(startDate, endDate);
        return ResponseEntity.ok(results);
    }

}
