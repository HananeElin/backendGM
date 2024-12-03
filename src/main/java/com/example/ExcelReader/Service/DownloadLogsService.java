package com.example.ExcelReader.Service;

import com.example.ExcelReader.Entity.DownloadLogs;
import com.example.ExcelReader.Repository.DownloadLogsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DownloadLogsService {

    private final DownloadLogsRepository downloadLogsRepository;

    public DownloadLogsService(DownloadLogsRepository downloadLogsRepository) {
        this.downloadLogsRepository = downloadLogsRepository;
    }

    // Method to retrieve the download logs
    public List<DownloadLogs> getDownloadHistory() {
        return downloadLogsRepository.findAll(); // Returns all the records
    }

    // search by uploaded filename
    public List<DownloadLogs> searchByUploadedFilename(String filename) {
        return downloadLogsRepository.findByUploadedFilenameContainingIgnoreCase(filename);
    }

    // search by downloaded filename
    public List<DownloadLogs> searchByDownloadedFilename(String downloadedFilename) {
        return downloadLogsRepository.findByDownloadedFilenameContainingIgnoreCase(downloadedFilename);
    }

    // search date exact
    public List<DownloadLogs> searchByDownloadDate(LocalDateTime downloadDate) {
        return downloadLogsRepository.findByCreatedAt(downloadDate);
    }

    //search between two  dates
    public List<DownloadLogs> searchByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return downloadLogsRepository.findByCreatedAtBetween(startDate, endDate);
    }

    // save data in download_logs table
    public void saveLog(String uploadedFilename, String downloadedFilename) {
        DownloadLogs log = new DownloadLogs(uploadedFilename, downloadedFilename);
        downloadLogsRepository.save(log);
    }
}
