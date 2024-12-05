package com.example.ExcelReader.Service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class BoaService {

    public byte[] generateInitialExcel(MultipartFile file) throws IOException {
        List<String[]> rawData = readFile(file); // Lire les données brutes du fichier
        String[] headers = new String[]
                {"Clé Contact", "Tel Gsm", "Tel Pro", "Tel Dom", "Campagne"};

        return generateExcel(rawData, headers);
    }

    public List<String[]> processFile(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split("\\|;");
                // Assurez-vous que chaque colonne est bien présente
                String cleContact = columns.length > 0 ? columns[0] : "Missing";
                String telGsm = columns.length > 1 ? columns[1] : "";
                String telPro = columns.length > 2 ? columns[2] : "";
                String telDom = columns.length > 3 ? columns[3] : "";
                String campagne = columns.length > 4 ? columns[4] : "Missing";

                // Valider les numéros de téléphone
                String validTelGsm = validatePhone(telGsm);
                String validTelPro = validatePhone(telPro);
                String validTelDom = validatePhone(telDom);

                rows.add(new String[]{cleContact, validTelGsm, validTelPro, validTelDom, campagne});
            }
        }
        return rows;
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "NA"; // Retourner "NA" pour les cellules vides
        }

        phone = phone.replaceAll("[^0-9]", ""); // Retirer tous les caractères non numériques

        // Gérer le préfixe +212, 212 ou 00212
        if (phone.startsWith("212")) {
            phone = "0" + phone.substring(3);
        } else if (phone.startsWith("00212")) {
            phone = "0" + phone.substring(5);
        }

        // Vérifier si le numéro est valide (10 chiffres)
        if (phone.length() == 10) {
            return phone;
        }

        return "NA"; // Retourner "NA" si le numéro est invalide
    }

    public byte[] generateExcel(List<String[]> data, String[] headers) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Contacts");

            // Écrire l'entête
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Écrire les données
            int rowIndex = 1;
            for (String[] row : data) {
                Row excelRow = sheet.createRow(rowIndex++);
                for (int i = 0; i < row.length; i++) {
                    excelRow.createCell(i).setCellValue(row[i]);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private List<String[]> readFile(MultipartFile file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                rows.add(line.split("\\|;"));
            }
        }
        return rows;
    }
}
