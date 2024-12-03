package com.example.ExcelReader.Repository;

import com.example.ExcelReader.Entity.DownloadLogs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DownloadLogsRepository extends JpaRepository<DownloadLogs, Long> {
    // search by uploaded filename
    List<DownloadLogs> findByUploadedFilenameContainingIgnoreCase(String uploadedFilename);

    // search by downloaded filename
    List<DownloadLogs> findByDownloadedFilenameContainingIgnoreCase(String downloadedFilename);

    // search by DownloadDate
    List<DownloadLogs> findByCreatedAt(LocalDateTime createdAt);

    // search  By DownloadDateBetween
    List<DownloadLogs> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}