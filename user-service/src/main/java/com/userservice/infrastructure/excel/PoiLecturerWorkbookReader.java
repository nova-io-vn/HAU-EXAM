package com.userservice.infrastructure.excel;

import com.userservice.application.port.out.LecturerWorkbookReader;
import java.io.*;
import java.util.*;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class PoiLecturerWorkbookReader implements LecturerWorkbookReader {
    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final long MAX = 5L * 1024 * 1024;
    private static final List<String> HEADERS = List.of("lecturerCode", "fullName", "phone", "academicRank", "academicDegree", "email", "facultyCode");

    @Override public List<Row> read(String fileName, String contentType, long size, InputStream input) {
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx")) throw new IllegalArgumentException("Only .xlsx files are supported");
        if (contentType != null && !contentType.isBlank() && !Set.of(XLSX, "application/octet-stream").contains(contentType)) throw new IllegalArgumentException("Invalid XLSX content type");
        if (size <= 0 || size > MAX) throw new IllegalArgumentException("XLSX file size is invalid");
        try {
            byte[] bytes = input.readAllBytes();
            if (bytes.length < 4 || bytes[0] != 'P' || bytes[1] != 'K') throw new IllegalArgumentException("Invalid XLSX file signature");
            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
                Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
                if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) throw new IllegalArgumentException("Workbook is empty");
                Map<String,Integer> columns = columns(sheet.getRow(0));
                var rows = new ArrayList<Row>();
                DataFormatter formatter = new DataFormatter(Locale.ROOT);
                for (int index = 1; index <= sheet.getLastRowNum(); index++) {
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(index);
                    if (row == null) continue;
                    String code = value(row, columns, "lecturerCode", formatter);
                    String name = value(row, columns, "fullName", formatter);
                    String email = value(row, columns, "email", formatter);
                    if (Stream.of(code, name, email).allMatch(String::isBlank)) continue;
                    rows.add(new Row(index + 1, code, name, value(row, columns, "phone", formatter),
                            value(row, columns, "academicRank", formatter), value(row, columns, "academicDegree", formatter),
                            email, value(row, columns, "facultyCode", formatter)));
                }
                if (rows.size() > 1000) throw new IllegalArgumentException("A workbook may contain at most 1000 lecturers");
                return List.copyOf(rows);
            }
        } catch (IllegalArgumentException exception) { throw exception; }
        catch (Exception exception) { throw new IllegalArgumentException("Cannot read XLSX workbook", exception); }
    }

    @Override public byte[] template() {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("GiangVien");
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            for (int i = 0; i < HEADERS.size(); i++) { header.createCell(i).setCellValue(HEADERS.get(i)); sheet.setColumnWidth(i, 22 * 256); }
            org.apache.poi.ss.usermodel.Row example = sheet.createRow(1);
            List<String> values = List.of("GV001", "Nguyễn Văn A", "0900000000", "NONE", "THS", "gv001@hau.edu.vn", "CNTT");
            for (int i = 0; i < values.size(); i++) example.createCell(i).setCellValue(values.get(i));
            workbook.write(output); return output.toByteArray();
        } catch (IOException exception) { throw new IllegalStateException("Cannot create XLSX template", exception); }
    }

    private Map<String,Integer> columns(org.apache.poi.ss.usermodel.Row header) {
        if (header == null) throw new IllegalArgumentException("Header row is required");
        DataFormatter formatter = new DataFormatter(Locale.ROOT); Map<String,Integer> result = new HashMap<>();
        for (Cell cell : header) result.put(formatter.formatCellValue(cell).trim().toLowerCase(Locale.ROOT), cell.getColumnIndex());
        for (String required : List.of("lecturerCode", "fullName", "email", "facultyCode")) if (!result.containsKey(required.toLowerCase(Locale.ROOT))) throw new IllegalArgumentException("Missing XLSX column: " + required);
        return result;
    }
    private String value(org.apache.poi.ss.usermodel.Row row, Map<String,Integer> columns, String key, DataFormatter formatter) {
        Integer index = columns.get(key.toLowerCase(Locale.ROOT)); return index == null ? "" : formatter.formatCellValue(row.getCell(index, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)).trim();
    }
}
