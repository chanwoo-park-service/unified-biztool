package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCampaignType;
import lombok.Builder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

@Builder
public record ExcelResponse(
        MetaCampaignType metaCampaignType,
        String campaignName,
        String budget,
        String setName,
        String creativeName,
        String shortUrl,
        String displayUrl,
        String uploadPage,
        String memo,
        String code,
        String shortUrlFlag,
        String landingUrl,
        String cafe24Url
) {

    public String getMetaCampaignType() {
        return metaCampaignType.getDescription();
    }

    public static ExcelResponse of(Row row) {
        return ExcelResponse.builder()
                .metaCampaignType(parseCampaignType(row.getCell(0)))
                .campaignName(getString(row.getCell(1)))
                .budget(getString(row.getCell(2)))
                .setName(getString(row.getCell(3)))
                .creativeName(getString(row.getCell(4)))
                .shortUrl(getString(row.getCell(5)))
                .displayUrl(getString(row.getCell(6)))
                .uploadPage(getString(row.getCell(7)))
                .memo(getString(row.getCell(8)))
                .code(getString(row.getCell(9)))
                .shortUrlFlag(getString(row.getCell(10)))
                .landingUrl(getString(row.getCell(11)))
                .cafe24Url(getString(row.getCell(12)))
                .build();
    }

    private static String getString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> evaluateFormula(cell);
            case BLANK -> "";
            case ERROR, _NONE -> null;
        };
    }

    private static String evaluateFormula(Cell cell) {
        try {
            FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
            CellValue cellValue = evaluator.evaluate(cell);

            return switch (cellValue.getCellType()) {
                case STRING -> cellValue.getStringValue().trim();
                case NUMERIC -> String.valueOf(cellValue.getNumberValue());
                case BOOLEAN -> String.valueOf(cellValue.getBooleanValue());
                default -> "";
            };
        } catch (Exception e) {
            return "";
        }
    }

    private static MetaCampaignType parseCampaignType(Cell cell) {
        return MetaCampaignType.from(getString(cell));
    }

}
