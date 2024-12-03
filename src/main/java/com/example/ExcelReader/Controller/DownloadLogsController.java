package com.example.ExcelReader.Controller;

import com.example.ExcelReader.Entity.DownloadLogs;
import com.example.ExcelReader.Service.DownloadLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/download_logs")
public class DownloadLogsController {

    @Autowired
    private DownloadLogsService downloadLogsService;

    // Endpoint to retrieve all logs
    @GetMapping("/")
    public ResponseEntity<List<DownloadLogs>> getDownloadHistory() {
        List<DownloadLogs> downloadLogs = downloadLogsService.getDownloadHistory();
        return ResponseEntity.ok(downloadLogs);
    }

    // Endpoint for searching by uploaded filename
    @GetMapping("/search-by-filename")
    public ResponseEntity<List<DownloadLogs>> searchByFilename(@RequestParam String filename) {
        List<DownloadLogs> results = downloadLogsService.searchByUploadedFilename(filename);
        return ResponseEntity.ok(results);
    }

    // Endpoint for searching by downloaded filename
    @GetMapping("/search-by-downloaded-filename")
    public ResponseEntity<List<DownloadLogs>> searchByDownloadedFilename(@RequestParam String downloadedFilename) {
        List<DownloadLogs> results = downloadLogsService.searchByDownloadedFilename(downloadedFilename);
        return ResponseEntity.ok(results);
    }

    // Endpoint for searching with an exact date
    @GetMapping("/search-by-date")
    public ResponseEntity<List<DownloadLogs>> searchByDownloadDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime downloadDate) {
        List<DownloadLogs> results = downloadLogsService.searchByDownloadDate(downloadDate);
        return ResponseEntity.ok(results);
    }

    // Endpoint for searching between two dates
    @GetMapping("/search-by-date-range")
    public ResponseEntity<List<DownloadLogs>> searchByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<DownloadLogs> results = downloadLogsService.searchByDateRange(startDate, endDate);
        return ResponseEntity.ok(results);
    }
}