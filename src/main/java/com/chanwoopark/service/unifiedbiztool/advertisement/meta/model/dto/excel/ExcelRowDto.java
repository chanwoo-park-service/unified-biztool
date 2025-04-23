package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.excel;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.AdAccount;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.Campaign;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.Page;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.Set;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCampaignObjective;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCampaignType;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCreativeFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Builder
@Getter
public class ExcelRowDto {
    private MetaCampaignObjective metaCampaignObjective;
    private String adAccountName;
    private MetaCampaignType metaCampaignType;
    @Setter
    private List<AdAccount> adAccountList;
    private String campaignName;
    @Setter
    private List<Campaign> campaignList;

    private Long budget;

    private LocalDate startDate;

    private LocalTime startTime;

    private String location;

    private String language;

    private String gender;

    private Integer minAge;

    private String maxAge;

    private String setName;
    @Setter
    private List<Set> setList;
    private String adMaterialName;
    private String uploadPage;

    @Setter
    private List<Page> uploadPageList;

    private MetaCreativeFormat metaCreativeFormat;
    private String landingUrl;
    private String displayUrl;
    private String defaultText;
    private String title;
    private String description;
    private String otherRequests;
    private String blank;

    @Setter
    private boolean pageResolved;
    @Setter
    private boolean accountResolved;
    @Setter
    private boolean campaignResolved;
    @Setter
    private boolean setResolved;

    //unused
    private String adCode;
    private String isShortUrlCreate;
    private String shortUrl;

    @Setter
    private String errorMessage;

    public static ExcelRowDto of(Row row) {
        return ExcelRowDto.builder()
                .metaCampaignObjective(MetaCampaignObjective.from(getString(row.getCell(0))))
                .adAccountName(getString(row.getCell(1)))
                .metaCampaignType(MetaCampaignType.valueOf(getString(row.getCell(2))))
                .campaignName(getString(row.getCell(3)))
                .budget(getLong(row.getCell(4)))
                .startDate(parseDate(row.getCell(5)))
                .startTime(parseTime(row.getCell(6)))
                .location(getString(row.getCell(7)))
                .language(getString(row.getCell(8)))
                .gender(getString(row.getCell(9)))
                .minAge(getInteger(row.getCell(10)))
                .maxAge(getString(row.getCell(11)))
                .setName(getString(row.getCell(12)))
                .adMaterialName(getString(row.getCell(13)))
                .uploadPage(getString(row.getCell(14)).replaceAll("\\.0", ""))
                .metaCreativeFormat(parseCreativeFormat(row.getCell(15)))
                .landingUrl(getString(row.getCell(16)))
                .displayUrl(getString(row.getCell(17)))
                .defaultText(getString(row.getCell(18)))
                .title(getString(row.getCell(19)))
                .description(getString(row.getCell(20)))
                .otherRequests(getString(row.getCell(21)))
                .blank(getString(row.getCell(22)))
                .adCode(getString(row.getCell(23)))
                .isShortUrlCreate(getString(row.getCell(24)))
                .shortUrl(getString(row.getCell(25)))
                .build();
    }

    public String getFirstCampaignId() {
        return campaignList != null && !campaignList.isEmpty() ? campaignList.get(0).getId() : null;
    }

    public String getFirstAccountId() {
        return adAccountList != null && !adAccountList.isEmpty() ? adAccountList.get(0).getId() : null;
    }

    public List<Integer> getGenders() {
        return switch (getGender()) {
            case "남자":
                yield List.of(1);
            case "여자":
                yield List.of(2);
            case "모든 성별":
                yield List.of(1, 2);
            default:
                throw new IllegalStateException("Unexpected value: " + getGender());
        };
    }

    public List<String> getGeoLocation() {
        return switch (getLocation()) {
            case "대한민국":
                yield List.of("KR");
            case "일본":
                yield List.of("JP");
            default:
                throw new IllegalStateException("Unexpected value: " + getGender());
        };
    }

    public List<Integer> getLocales() {
        return switch (getLanguage()) {
            case "한국어":
                yield List.of(3);
            case "영어":
                yield List.of(4);
            default:
                throw new IllegalStateException("Unexpected value: " + getGender());
        };
    }

    private static LocalDate parseDate(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate();
                } else {
                    double raw = cell.getNumericCellValue();
                    String rawStr = String.format("%06d", (int) raw); // "250422"
                    int year = 2000 + Integer.parseInt(rawStr.substring(0, 2));
                    int month = Integer.parseInt(rawStr.substring(2, 4));
                    int day = Integer.parseInt(rawStr.substring(4, 6));
                    yield LocalDate.of(year, month, day);
                }
            }
            case STRING -> {
                try {
                    yield LocalDate.parse(cell.getStringCellValue().trim());
                } catch (Exception e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private static LocalTime parseTime(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case _NONE, BLANK, BOOLEAN, ERROR -> null;
            case NUMERIC -> cell.getLocalDateTimeCellValue().toLocalTime().withSecond(0).withNano(0);
            case STRING -> {
                try {
                    String timeStr = cell.getStringCellValue().trim();
                    yield LocalTime.parse(timeStr);
                } catch (Exception e) {
                    yield null;
                }
            }
            case FORMULA -> {
                try {
                    FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(cell);
                    if (cellValue.getCellType() == CellType.NUMERIC) {
                        yield cell.getLocalDateTimeCellValue().toLocalTime().withSecond(0).withNano(0);
                    } else if (cellValue.getCellType() == CellType.STRING) {
                        yield LocalTime.parse(cellValue.getStringValue().trim());
                    } else {
                        yield null;
                    }
                } catch (Exception e) {
                    yield null;
                }
            }
        };
    }

    private static MetaCreativeFormat parseCreativeFormat(Cell cell) {
        String value = getString(cell);
        return MetaCreativeFormat.fromDescription(value);
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

    private static Integer getInteger(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Integer.parseInt(cell.getStringCellValue().replace(",", "").trim());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            case FORMULA -> {
                try {
                    FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(cell);
                    yield switch (cellValue.getCellType()) {
                        case NUMERIC -> (int) cellValue.getNumberValue();
                        case STRING -> {
                            try {
                                yield Integer.parseInt(cellValue.getStringValue().replace(",", "").trim());
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

}
