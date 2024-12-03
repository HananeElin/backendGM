package com.example.ExcelReader.Repository;

import com.example.ExcelReader.Entity.Download_logs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface Download_logs_repository extends JpaRepository<Download_logs, Long> {
    // search by File Name
    List<Download_logs> findByFileNameContainingIgnoreCase(String fileName);

    // search by DownloadDate
    List<Download_logs> findByDownloadDate(LocalDateTime downloadDate);

    // search  By DownloadDateBetween
    List<Download_logs> findByDownloadDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}

