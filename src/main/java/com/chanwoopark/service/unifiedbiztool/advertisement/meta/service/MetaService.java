package com.chanwoopark.service.unifiedbiztool.advertisement.meta.service;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception.InvalidExcelFormatException;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.ExcelResponse;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.ExcelRowDto;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.MetaIdResponse;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.MetaId;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.*;
import com.chanwoopark.service.unifiedbiztool.common.http.HttpClientHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class MetaService {

    private final HttpClientHelper httpClientHelper;

    @Value("${app.meta.ads.access-token}")
    private String accessToken;

    private final String META_URL = "https://graph.facebook.com";

    public List<ExcelResponse> processExcel(MultipartFile file) {
        List<CompletableFuture<ExcelResponse>> futures = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int dataStartRowIndex = 3;

            for (int i = dataStartRowIndex; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                ExcelRowDto dto = ExcelRowDto.of(row);
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        return processMetaIdentifiers(dto);
                    } catch (Exception e) {
                        log.error("행 처리 중 오류 발생", e);
                        return ExcelResponse.of(dto);
                    }
                }));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .toList();

        } catch (IOException e) {
            throw new IllegalArgumentException("엑셀 파일 처리 중 오류 발생", e);
        }
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

    private ExcelResponse processMetaIdentifiers(@Valid ExcelRowDto excelRowDto) throws JsonProcessingException {
        String accountIdResponse = httpClientHelper.get(
                META_URL
                        + "/v22.0/me/adaccounts?access_token="
                        + accessToken
                        + "&fields=name,id"
        );
        excelRowDto.setAdvertiseAccountIdList(parseIdList(accountIdResponse, excelRowDto.getAdvertiseAccountName()));
        if (excelRowDto.getAdvertiseAccountIdList() == null || excelRowDto.getAdvertiseAccountIdList().size() != 1) {
            return ExcelResponse.of(excelRowDto);
        }
        String accountId = excelRowDto.getFirstAccountId();
        excelRowDto.setAccountResolved(true);

        excelRowDto.setCampaignIdList(
                getOrCreateIdList(
                        META_URL
                                + "/v22.0/"
                                + Objects.requireNonNull(accountId)
                                + "/campaigns?access_token="
                                + accessToken
                                + "&fields=name,id",
                        excelRowDto.getCampaignName(),
                        META_URL
                                + "/v22.0/"
                                + accountId
                                + "/campaigns",
                        form -> form
                                .with("name", excelRowDto.getCampaignName())
                                .with("objective", MetaCampaignObjective.OUTCOME_SALES.name())
                                .with("status", MetaAdStatus.PAUSED.name())
                                .with("access_token", accessToken)
                                .with("special_ad_categories", MetaSpecialAdCategory.NONE.name())
        ));
        excelRowDto.setCampaignResolved(true);
        excelRowDto.setSetIdList(
                getOrCreateIdList(
                        META_URL
                                + "/v22.0/"
                                + accountId
                                + "/adsets?access_token="
                                + accessToken
                                + "&fields=name,id",
                        excelRowDto.getSetName(),
                        META_URL
                                + "/v22.0/"
                                + accountId
                                + "/adsets",
                        form -> form
                                .with("name", excelRowDto.getSetName())
                                .with("optimization_goal", MetaOptimizationGoal.REACH.name())
                                .with("billing_event", MetaBillingEvent.IMPRESSIONS.name())
                                .with("bid_amount", "1500")
                                .with("daily_budget", String.valueOf(excelRowDto.getBudget()))
                                .with("campaign_id", excelRowDto.getFirstCampaignId())
                                .with("targeting", "{\"geo_locations\":{\"countries\":[\"KR\"]}}")
                                .with("status", MetaAdStatus.PAUSED.name())
                                .with("access_token", accessToken)
        ));
        excelRowDto.setSetResolved(true);
        return ExcelResponse.of(excelRowDto);
    }

    private String getIdFromJson(String json) throws JsonProcessingException {
        return new ObjectMapper().readTree(json).get("id").asText();
    }

    private List<String> parseIdList(String json, String targetName) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        MetaIdResponse response = objectMapper.readValue(json, MetaIdResponse.class);

        return response.getData().stream()
                .filter(metaId -> metaId.getName() != null
                        && metaId.getName().trim().equals(targetName))
                .map(MetaId::getId)
                .distinct()
                .toList();
    }

    private List<String> getOrCreateIdList(
            String getUrl,
            String nameToFind,
            String createUrl,
            Consumer<BodyInserters.FormInserter<String>> creator
    ) throws JsonProcessingException {
        String getResponse = httpClientHelper.get(getUrl);
        List<String> idList = parseIdList(getResponse, nameToFind);
        if (idList != null && !idList.isEmpty()) return idList;

        String postResponse = httpClientHelper.postForm(createUrl, creator);
        return List.of(getIdFromJson(postResponse));
    }


}
