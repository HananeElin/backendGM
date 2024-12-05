package com.example.ExcelReader.Controller;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class BoaController {

    @PostMapping("/upload_txt")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Le fichier est vide ou absent !");
        }

        try {
            // Traiter le fichier TXT
            List<String[]> data = processFile(file);

            // Générer un fichier Excel
            byte[] excelBytes = generateExcel(data);

            // return the file in output
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("processed_data.xlsx").build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelBytes);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors du traitement du fichier : " + e.getMessage());
        }
    }

    private List<String[]> processFile(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));

        String line;
        while ((line = br.readLine()) != null) {
            String[] columns = line.split("\\|;");

            // cells validation
            if (columns.length < 5) {
                rows.add(new String[] { "Invalid Row", "", "" });
                continue;
            }

            // phone trait
            String phone = processPhone(columns);

            // if there is no valid number, ignore
            if (phone.equals("Aucun numéro")) {
                rows.add(new String[] { columns[0], "Aucun numéro", columns[4] });
                continue;
            }

            // Ajouter les données nettoyées
            rows.add(new String[] { columns[0], phone, columns[4] });
        }
        return rows;
    }

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
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        return out.toByteArray();
    }
}
