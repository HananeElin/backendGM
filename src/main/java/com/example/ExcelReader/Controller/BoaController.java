package com.example.ExcelReader.Controller;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class BoaController {

    @PostMapping("/upload_txt")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No file uploaded. Please upload a valid .txt file.");
        }
        if (!file.getOriginalFilename().endsWith(".txt")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file type. Please upload a .txt file.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             XSSFWorkbook workbook = new XSSFWorkbook();
             java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream()) {

            // Create a new sheet in the Excel file
            Sheet sheet = workbook.createSheet("Sheet");

            String line;
            int rowNum = 0;

            // Read each line from the text file and populate the Excel sheet
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split("\t"); // Assuming tab-separated values in the .txt file
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(columns[i]);
                }
            }

            workbook.write(outputStream);

            // Create a unique filename for the output
            String outputFilename = "generated_excel_" + System.currentTimeMillis() + ".xlsx";

            // Prepare response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + outputFilename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(outputStream.toByteArray());

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while processing the file: " + e.getMessage());
        }
    }

    // This method can process the .txt file
    private List<String[]> processFile(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            String[] columns = line.split("\\|;");

            // Validation des colonnes
            if (columns.length < 5) {
                rows.add(new String[] { "Invalid Row", "", "" });
                continue;
            }

            // Traiter les numéros de téléphone
            String phone = processPhone(columns);

            // Si aucun numéro valide, ignorer ou marquer la ligne
            if (phone.equals("Aucun numéro")) {
                rows.add(new String[] { columns[0], "Aucun numéro", columns[4] });
                continue;
            }

            // Ajouter les données nettoyées
            rows.add(new String[] { columns[0], phone, columns[4] });
        }
        return rows;
    }

    // This method cleans up the phone numbers and returns a valid one
    private String processPhone(String[] columns) {
        for (int i = 1; i <= 3; i++) {
            if (i < columns.length && columns[i] != null && !columns[i].isEmpty()) {
                // Nettoyer les caractères non numériques
                String phone = columns[i].replaceAll("\\D", "");

                // Remplacer +212 par 06
                if (phone.startsWith("212")) {
                    phone = "06" + phone.substring(3);
                }

                // Valider la longueur (10 chiffres)
                if (phone.length() == 10) {
                    return phone;
                }
            }
        }
        return "Aucun numéro";
    }

    // This method generates the Excel file from processed data
    private byte[] generateExcel(List<String[]> data) throws IOException {
        // Création du fichier Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Contacts");

        // Créer les en-têtes
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("CLE_CONTACT");
        headerRow.createCell(1).setCellValue("NUMERO_TELEPHONE");
        headerRow.createCell(2).setCellValue("CAMPAGNE");

        // Ajouter les données
        int rowNum = 1;
        for (String[] row : data) {
            Row excelRow = sheet.createRow(rowNum++);
            for (int col = 0; col < row.length; col++) {
                excelRow.createCell(col).setCellValue(row[col]);
            }
        }

        // Retourner le fichier Excel sous forme de tableau binaire
        java.io.ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}
