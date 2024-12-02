package com.example.ExcelReader.Repository;

import com.example.ExcelReader.Entity.Download_logs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadHistoryRepository extends JpaRepository<Download_logs, Long> {
}
