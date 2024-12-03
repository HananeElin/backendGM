
package com.example.ExcelReader.Controller;


import com.example.ExcelReader.Service.ExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/excel")
public class ExcelController {


    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndProcessExcel(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "telephone", required = false, defaultValue = "0") int telephoneCell,
        @RequestParam(value = "telGestionnaire", required = false, defaultValue = "1") int telGestionnaireCell,
        @RequestParam(value = "amount", required = false, defaultValue = "2") int amountCell
    ) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No file uploaded. Please upload a valid Excel file.");
        }

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file type. Please upload an Excel file with .xlsx extension.");
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream());
             XSSFWorkbook outputWorkbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet inputSheet = workbook.getSheetAt(0); // Première feuille du fichier d'entrée

            Sheet validSheet = outputWorkbook.createSheet("Valid Rows");
            Sheet invalidSheet = outputWorkbook.createSheet("Invalid Rows");

            ExcelService.normalizeSheet(inputSheet, validSheet, invalidSheet, telephoneCell, telGestionnaireCell, amountCell);

            outputWorkbook.write(outputStream);

            HttpHeaders headers = new HttpHeaders();
            long currentTimeMillis = System.currentTimeMillis();
            headers.add("Content-Disposition", "attachment; filename=processed_file_"+currentTimeMillis+".xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(outputStream.toByteArray());

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the file: " + e.getMessage());
        }
    }



    @PostMapping("/upload/multi-files")
    public ResponseEntity<byte[]> uploadAndProcessMultiFilesExcel(
        @RequestParam("file") MultipartFile file,
        @RequestParam("telephone") int telephoneCell,
        @RequestParam("telGestionnaire") int telGestionnaireCell,
        @RequestParam("amount") int amountCell
    ) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No file uploaded. Please upload a valid Excel file.".getBytes());
        }

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file type. Please upload an Excel file with .xlsx extension.".getBytes());
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream());
             XSSFWorkbook validWorkbook = new XSSFWorkbook();
             XSSFWorkbook invalidWorkbook = new XSSFWorkbook();
             ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
             java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(zipOutputStream)) {

            System.out.println("Received telephone cell: " + telephoneCell);
            System.out.println("Received telGestionnaire cell: " + telGestionnaireCell);
            System.out.println("Received amount cell: " + amountCell);

            // Process Excel file
            Sheet inputSheet = workbook.getSheetAt(0); // First sheet of the input file
            Sheet validSheet = validWorkbook.createSheet("Valid Rows");
            Sheet invalidSheet = invalidWorkbook.createSheet("Invalid Rows");
            ExcelService.normalizeSheet(inputSheet, validSheet, invalidSheet, telephoneCell, telGestionnaireCell, amountCell);
            // Get the original file name (old file name)
            String originalFileName = file.getOriginalFilename();

            long currentTimeMillis = System.currentTimeMillis();

            // Add valid workbook to ZIP
            try (ByteArrayOutputStream validOutputStream = new ByteArrayOutputStream()) {
                validWorkbook.write(validOutputStream);
                ExcelService.addFileToZip("valid_file_"+ originalFileName + "_"+currentTimeMillis+".xlsx", validOutputStream.toByteArray(), zos);
            }

            // Add invalid workbook to ZIP
            try (ByteArrayOutputStream invalidOutputStream = new ByteArrayOutputStream()) {
                invalidWorkbook.write(invalidOutputStream);
                ExcelService.addFileToZip("invalid_file_"+ originalFileName + "_"+currentTimeMillis+".xlsx", invalidOutputStream.toByteArray(), zos);
            }

            zos.close();

            // Prepare response
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=processed_files_"+ originalFileName + "_"+currentTimeMillis+".zip");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipOutputStream.toByteArray());

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("An error occurred while processing the file: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/detect-columns")
    public ResponseEntity<Map<String, Object>> detectColumns(@RequestParam("file") MultipartFile file) {
        try {
            // Parse the uploaded file
            Map<String, Object> response = ExcelService.detectColumns(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }



}
   