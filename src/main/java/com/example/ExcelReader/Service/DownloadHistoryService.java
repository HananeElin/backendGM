package com.example.ExcelReader.Service;

import com.example.ExcelReader.Entity.Download_logs;
import com.example.ExcelReader.Repository.DownloadHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DownloadHistoryService {

    private final DownloadHistoryRepository downloadHistoryRepository;

    public DownloadHistoryService(DownloadHistoryRepository downloadHistoryRepository) {
        this.downloadHistoryRepository = downloadHistoryRepository;
    }

    // Méthode pour récupérer l'historique des téléchargements
    public List<Download_logs> getDownloadHistory() {
        return downloadHistoryRepository.findAll(); // Retourne tous les enregistrements
    }
}
