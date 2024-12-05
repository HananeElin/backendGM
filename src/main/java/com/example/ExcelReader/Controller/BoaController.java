package com.example.ExcelReader.Controller;


import com.example.ExcelReader.Service.BoaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class BoaController {

    @Autowired
    private BoaService boaService;

    @PostMapping("/upload_txt")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null || !file.getOriginalFilename().endsWith(".txt")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file. Please upload a non-empty .txt file.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            // Read the header
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The file is empty.");
            }

            String[] headers = headerLine.split("\\|;");

            // Process the file data
            List<String[]> processedData = boaService.processFile(file);

            // Generate Excel
            byte[] excelData = boaService.generateExcel(processedData, headers);

            // Response setup
            String outputFilename = "generated_excel_" + System.currentTimeMillis() + ".xlsx";
            HttpHeaders headersResp = new HttpHeaders();
            headersResp.add("Content-Disposition", "attachment; filename=" + outputFilename);

            return ResponseEntity.ok()
                    .headers(headersResp)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelData);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the file: " + e.getMessage());
        }
    }
}
