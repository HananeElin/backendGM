package com.example.ExcelReader.Service;

import com.example.ExcelReader.Entity.Download_logs;
import com.example.ExcelReader.Repository.Download_logs_repository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class Download_logs_service {

    private final Download_logs_repository downloadLogsrepository;

    public Download_logs_service(Download_logs_repository downloadLogsrepository) {
        this.downloadLogsrepository = downloadLogsrepository;
    }

    // Méthode pour récupérer l'historique des téléchargements
    public List<Download_logs> getDownloadHistory() {
        return downloadLogsrepository.findAll(); // Retourne tous les enregistrements
    }
    // search by FileName
    public List<Download_logs> searchByFileName(String fileName) {
        return downloadLogsrepository.findByFileNameContainingIgnoreCase(fileName);
    }

    // search date exact
    public List<Download_logs> searchByDownloadDate(LocalDateTime downloadDate) {
        return downloadLogsrepository.findByDownloadDate(downloadDate);
    }

    //search between two  dates
    public List<Download_logs> searchByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return downloadLogsrepository.findByDownloadDateBetween(startDate, endDate);
    }
}
