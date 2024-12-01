
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/excel")
public class ExcelController {
    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndProcessExcel(@RequestParam("file") MultipartFile file) {
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

            normalizeSheet(inputSheet, validSheet, invalidSheet);

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
    public ResponseEntity<byte[]> uploadAndProcessMultiFilesExcel(@RequestParam("file") MultipartFile file) {
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

            // Process Excel file
            Sheet inputSheet = workbook.getSheetAt(0); // First sheet of the input file
            Sheet validSheet = validWorkbook.createSheet("Valid Rows");
            Sheet invalidSheet = invalidWorkbook.createSheet("Invalid Rows");
            normalizeSheet(inputSheet, validSheet, invalidSheet);

            long currentTimeMillis = System.currentTimeMillis();

            // Add valid workbook to ZIP
            try (ByteArrayOutputStream validOutputStream = new ByteArrayOutputStream()) {
                validWorkbook.write(validOutputStream);
                addFileToZip("valid_file_"+currentTimeMillis+".xlsx", validOutputStream.toByteArray(), zos);
            }

            // Add invalid workbook to ZIP
            try (ByteArrayOutputStream invalidOutputStream = new ByteArrayOutputStream()) {
                invalidWorkbook.write(invalidOutputStream);
                addFileToZip("invalid_file_"+currentTimeMillis+".xlsx", invalidOutputStream.toByteArray(), zos);
            }

            zos.close();

            // Prepare response
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=processed_files_"+currentTimeMillis+".zip");
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

    private void normalizeSheet(Sheet inputSheet, Sheet validSheet, Sheet invalidSheet) {
        boolean isHeaderRow = true;
        Set<String> phoneSet = new HashSet<>();
        int validRowIndex = 0;
        int invalidRowIndex = 0;

        for (Row row : inputSheet) {
            if (isHeaderRow) {
                // Copier l'en-tête
                copyRow(row, validSheet.createRow(validRowIndex++));
                copyRow(row, invalidSheet.createRow(invalidRowIndex++));
                isHeaderRow = false;
                continue;
            }
            Cell registerCell = row.getCell(1);

            // Vérifier et traiter le registre
            boolean registerValid = registerCell != null && registerCell.getCellType() == CellType.STRING;
            String rawRegister = registerValid ? registerCell.getStringCellValue() : null;
            String normalizedRegister = registerValid ? processRegister(rawRegister) : "Invalid";

            Cell phoneCell = row.getCell(0); // Supposé le num en première cell
            if (phoneCell == null || phoneCell.getCellType() != CellType.STRING) {
                copyRow(row, invalidSheet.createRow(invalidRowIndex++));
                continue;
            }

            String rawPhone = phoneCell.getStringCellValue();
            String normalizedPhone = normalizePhoneNumber(rawPhone);

            if (normalizedPhone.equals("Invalid") || !phoneSet.add(normalizedPhone)) {
                copyRow(row, invalidSheet.createRow(invalidRowIndex++));
            } else {
                phoneCell.setCellValue(normalizedPhone); // Mettre à jour le numéro normalisé
                processAmount(row); // Normaliser les montants
                copyRow(row, validSheet.createRow(validRowIndex++));
            }
        }
    }
    private String processRegister(String register) {
        String cleanedRegister = register.replaceAll("[^0-9]", "");

        if (cleanedRegister.length() < 9 || cleanedRegister.length() > 10) {
            return "Invalid";
        }

        if (cleanedRegister.startsWith("5")||cleanedRegister.startsWith("6")||
                cleanedRegister.startsWith("7")||cleanedRegister.startsWith("8")) {
            return "0" + cleanedRegister;
        }

        return "Invalid";
    }

    private void processAmount(Row row) {
        Cell amountCell = row.getCell(2); // Supposé le montant en troisième cellule
        if (amountCell != null && amountCell.getCellType() == CellType.NUMERIC) {
            int intAmount = (int) amountCell.getNumericCellValue();// Convertir les montants en entier
            amountCell.setCellValue(intAmount);
        }
    }

    private String normalizePhoneNumber(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        if (cleaned.length() < 9 || cleaned.length() > 12) {
            return "Invalid";
        }

        if (cleaned.startsWith("212")) {
            return "0" + cleaned.substring(3);
        }

        if (cleaned.startsWith("05") || cleaned.startsWith("06") ||
                cleaned.startsWith("07") || cleaned.startsWith("08")) {
            return cleaned;
        }


        return "Invalid";
    }

    private void copyRow(Row sourceRow, Row targetRow) {
        for (int i = 0; i < sourceRow.getPhysicalNumberOfCells(); i++) {
            Cell sourceCell = sourceRow.getCell(i);
            Cell targetCell = targetRow.createCell(i);

            if (sourceCell != null) {
                switch (sourceCell.getCellType()) {
                    case STRING -> targetCell.setCellValue(sourceCell.getStringCellValue());
                    case NUMERIC -> targetCell.setCellValue(sourceCell.getNumericCellValue());
                    default -> targetCell.setCellValue(sourceCell.toString());
                }
            }
        }
    }

    /**
     * Helper method to add a file to the ZIP archive.
     */
    private void addFileToZip(String fileName, byte[] fileContent, java.util.zip.ZipOutputStream zos) throws IOException {
        java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(fileName);
        zos.putNextEntry(entry);
        zos.write(fileContent);
        zos.closeEntry();
    }

}
   