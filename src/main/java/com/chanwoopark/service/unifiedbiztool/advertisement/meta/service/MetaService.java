package com.chanwoopark.service.unifiedbiztool.advertisement.meta.service;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception.InvalidExcelFormatException;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.ExcelResponse;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaExcelColumns;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class MetaService {
    public List<ExcelResponse> processExcel(MultipartFile file) {
        List<ExcelResponse> result = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int dataStartRowIndex = 3;

            for (int i = dataStartRowIndex; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                ExcelResponse response = ExcelResponse.of(row);
                result.add(response);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("엑셀 파일 처리 중 오류 발생", e);
        }

        return result;
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK && !getRawCellValue(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getRawCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    public void validateExcel(MultipartFile file) throws IOException {
        List<String> expectedHeaders = MetaExcelColumns.headers();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(2);

            if (headerRow == null) {
                throw new InvalidExcelFormatException("엑셀 파일에 헤더가 존재하지 않습니다.");
            }

            OptionalInt invalidIndex = IntStream.range(0, expectedHeaders.size())
                    .filter(i -> {
                        Cell cell = headerRow.getCell(i);
                        return cell == null || !expectedHeaders.get(i).equals(cell.getStringCellValue().trim());
                    })
                    .findFirst();

            if (invalidIndex.isPresent()) {
                int idx = invalidIndex.getAsInt();
                throw new InvalidExcelFormatException(
                        String.format("%d번째 열은 '%s' 이어야 합니다.", idx + 1, expectedHeaders.get(idx))
                );
            }

        }
    }
}
