package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCampaignType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;

@Builder
@Getter
public class ExcelRowDto {
    private MetaCampaignType metaCampaignType;
    private String advertiseAccountName;
    @Setter
    private List<String> advertiseAccountIdList;
    private String campaignName;
    @Setter
    private List<String> campaignIdList;

    private Long budget;

    private String setName;
    @Setter
    private List<String> setIdList;
    private String advertisementName;
    private String shortUrl;
    private String displayUrl;
    private String uploadPage;
    private String memo;
    private String code;
    private String shortUrlFlag;
    private String landingUrl;
    private String cafe24Url;

    @Setter
    private boolean accountResolved;
    @Setter
    private boolean campaignResolved;
    @Setter
    private boolean setResolved;

    public static ExcelRowDto of(Row row) {
        return ExcelRowDto.builder()
                .metaCampaignType(parseCampaignType(row.getCell(0)))
                .advertiseAccountName(getString(row.getCell(1)))
                .campaignName(getString(row.getCell(2)))
                .budget(getLong(row.getCell(3)))
                .setName(getString(row.getCell(4)))
                .advertisementName(getString(row.getCell(5)))
                .shortUrl(getString(row.getCell(6)))
                .displayUrl(getString(row.getCell(7)))
                .uploadPage(getString(row.getCell(8)))
                .memo(getString(row.getCell(9)))
                .code(getString(row.getCell(10)))
                .shortUrlFlag(getString(row.getCell(11)))
                .landingUrl(getString(row.getCell(12)))
                .cafe24Url(getString(row.getCell(13)))
                .build();
    }

    public String getFirstCampaignId() {
        return campaignIdList != null && !campaignIdList.isEmpty() ? campaignIdList.get(0) : null;
    }

    public String getFirstAccountId() {
        return advertiseAccountIdList != null && !advertiseAccountIdList.isEmpty() ? advertiseAccountIdList.get(0) : null;
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

    private static Long getLong(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Long.parseLong(cell.getStringCellValue().replace(",", "").trim());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            case FORMULA -> {
                try {
                    FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(cell);
                    yield switch (cellValue.getCellType()) {
                        case NUMERIC -> (long) cellValue.getNumberValue();
                        case STRING -> {
                            try {
                                yield Long.parseLong(cellValue.getStringValue().replace(",", "").trim());
                            } catch (NumberFormatException e) {
                                yield null;
                            }
                        }
                        default -> null;
                    };
                } catch (Exception e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private static MetaCampaignType parseCampaignType(Cell cell) {
        return MetaCampaignType.from(getString(cell));
    }

}
