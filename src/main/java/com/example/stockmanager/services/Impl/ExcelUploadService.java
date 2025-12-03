package com.example.stockmanager.services.Impl;

import com.example.stockmanager.entities.Stock;
import com.example.stockmanager.entities.ContainerWeights;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExcelUploadService {
    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String XLSX_EXTENSION = ".xlsx";
    private static final List<String> EXPECTED_HEADERS = List.of("code", "name", "quantity", "container_name", "weight");

    public static boolean isValidExcelFile(MultipartFile file) {
        if (file == null) {
            return false;
        }
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        boolean matchesMime = EXCEL_CONTENT_TYPE.equalsIgnoreCase(contentType);
        boolean matchesExtension = filename != null && filename.toLowerCase(Locale.ROOT).endsWith(XLSX_EXTENSION);
        return matchesMime || matchesExtension;
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK &&
                    !(cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty())) {
                return false;
            }
        }
        return true;
    }

    public static List<Stock> getStockDataFromExcel(InputStream inputStream) throws IOException {
        List<Stock> stockList = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
            if (sheet == null) {
                throw new IllegalArgumentException("The uploaded workbook does not contain any sheets.");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null || isRowEmpty(headerRow)) {
                throw new IllegalArgumentException("The first row must contain the headers: " + EXPECTED_HEADERS);
            }

            Map<String, Integer> headerIndex = extractHeaderIndex(headerRow);
            if (!headerIndex.keySet().containsAll(EXPECTED_HEADERS)) {
                throw new IllegalArgumentException("Excel file must contain the columns: " + EXPECTED_HEADERS);
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isRowEmpty(row)) {
                    continue;
                }

                Stock stock = new Stock();
                String code = getOptionalString(row, headerIndex.get("code"));
                stock.setCode(code != null && !code.isBlank() ? code : null);

                String name = getRequiredString(row, headerIndex.get("name"), rowIndex);
                Integer quantity = getRequiredInteger(row, headerIndex.get("quantity"), rowIndex);
                String containerName = getRequiredString(row, headerIndex.get("container_name"), rowIndex);
                ContainerWeights weight = getRequiredWeight(row, headerIndex.get("weight"), rowIndex);

                stock.setName(name);
                stock.setQuantity(quantity);
                stock.setContainer_name(containerName);
                stock.setWeight(weight);
                stockList.add(stock);
            }
        }

        return stockList;
    }

    private static Map<String, Integer> extractHeaderIndex(Row headerRow) {
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
            Cell cell = headerRow.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell == null || cell.getCellType() != CellType.STRING) {
                continue;
            }
            String headerName = cell.getStringCellValue().trim().toLowerCase(Locale.ROOT);
            headerIndex.put(headerName, cellIndex);
        }
        return headerIndex;
    }

    private static String getOptionalString(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return null;
    }

    private static String getRequiredString(Row row, int cellIndex, int rowIndex) {
        String value = getOptionalString(row, cellIndex);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": mandatory text value missing.");
        }
        return value.trim();
    }

    private static Integer getRequiredInteger(Row row, int cellIndex, int rowIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": quantity is missing.");
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        if (cell.getCellType() == CellType.STRING) {
            try {
                return Integer.parseInt(cell.getStringCellValue().trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": quantity must be numeric.");
            }
        }
        throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": quantity must be numeric.");
    }

    private static ContainerWeights getRequiredWeight(Row row, int cellIndex, int rowIndex) {
        String value = getOptionalString(row, cellIndex);
        if (value == null) {
            throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": weight is missing.");
        }
        try {
            return ContainerWeights.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Row " + (rowIndex + 1) + ": weight must be one of " + Arrays.toString(ContainerWeights.values()));
        }
    }
}
