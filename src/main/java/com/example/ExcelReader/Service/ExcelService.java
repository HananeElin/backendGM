package com.example.ExcelReader.Service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ExcelService {

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

