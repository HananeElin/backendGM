package com.example.ExcelReader.Service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ExcelService {

    public static void normalizeSheet(Sheet inputSheet, Sheet validSheet, Sheet invalidSheet) {
        boolean isHeaderRow = true;
        Set<String> phoneSet = new HashSet<>();
        int validRowIndex = 0;
        int invalidRowIndex = 0;

        for (Row row : inputSheet) {
            if (isHeaderRow) {
                // Copy the header
                copyRow(row, validSheet.createRow(validRowIndex++));
                copyRow(row, invalidSheet.createRow(invalidRowIndex++));
                isHeaderRow = false;
                continue;
            }
            Cell registerCell = row.getCell(1);

            // Check and process the register
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
                phoneCell.setCellValue(normalizedPhone); // Update the normalized number
                processAmount(row); // Normalize the amounts
                copyRow(row, validSheet.createRow(validRowIndex++));
            }
        }
    }
    public static String processRegister(String register) {
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

    public static void processAmount(Row row) {
        Cell amountCell = row.getCell(2); // Assume the amount is in the third cell
        if (amountCell != null && amountCell.getCellType() == CellType.NUMERIC) {
            int intAmount = (int) amountCell.getNumericCellValue(); // Convert the amounts to integers
            amountCell.setCellValue(intAmount);
        }
    }

    public static String normalizePhoneNumber(String phoneNumber) {
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

    public static void copyRow(Row sourceRow, Row targetRow) {
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
    public static void addFileToZip(String fileName, byte[] fileContent, java.util.zip.ZipOutputStream zos) throws IOException {
        java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(fileName);
        zos.putNextEntry(entry);
        zos.write(fileContent);
        zos.closeEntry();
    }

    public static Map<String, Object> detectColumns(MultipartFile file) throws IOException {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> columnTypes = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next(); // Skip header row
            }

            // Analyze first data row to detect types
            if (rowIterator.hasNext()) {
                Row dataRow = rowIterator.next();

                for (Cell cell : dataRow) {
                    Map<String, Object> column = new HashMap<>();
                    column.put("cell", cell.getColumnIndex());
                    column.put("type", detectCellType(cell));
                    columnTypes.add(column);
                }
            }
        }

        response.put("data", columnTypes);
        return response;
    }

    private static String getCellValueAsString(Cell cell) {
        // Check if the cell is numeric
        if (cell.getCellType() == CellType.NUMERIC) {
            // Convert the numeric value to a string to avoid scientific notation
            double numericValue = cell.getNumericCellValue();

            /* Check if it's a large number (likely a phone number)
             * If the number looks like a phone number, return it as a string (no scientific notation)
             */
            return String.format("%.0f", numericValue);
        } else {
            // If it's not numeric, return the cell's string value
            return cell.toString().trim();
        }
    }

    private static String detectCellType(Cell cell) {
        String cellValue = getCellValueAsString(cell);

        // Detect phone numbers
        if (cellValue.matches("^([5678])\\d{8}$") ||
                cellValue.matches("^212\\d{8}$") ||
                cellValue.matches("^\\+212\\d{8}$")) {
            return "phoneNumber";
        }

        // Detect amounts (numeric values with optional decimals)
        else if (cellValue.matches("^\\d+(\\.\\d{1,2})?$")) {
            return "amount";
        }

        // Detect registration values (alphanumeric values)
        else if (cellValue.matches("^[A-Za-z0-9]+$")) {
            return "registration";
        } else {
            return "unknown";
        }
    }
}

