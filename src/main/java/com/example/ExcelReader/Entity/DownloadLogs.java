package com.example.ExcelReader.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "download_logs")
public class DownloadLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uploadedFilename;
    private String downloadedFilename;
    private LocalDateTime createdAt;

    public DownloadLogs(String uploadedFilename, String downloadedFilename) {
        this.uploadedFilename = uploadedFilename;
        this.downloadedFilename = downloadedFilename;
        this.createdAt = LocalDateTime.now();
    }
}