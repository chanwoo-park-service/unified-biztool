package com.chanwoopark.service.unifiedbiztool.advertisement.meta.service;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception.HttpClientException;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception.InvalidExcelFormatException;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.*;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.*;
import com.chanwoopark.service.unifiedbiztool.common.http.HttpClientHelper;
import com.chanwoopark.service.unifiedbiztool.common.model.enums.Platform;
import com.chanwoopark.service.unifiedbiztool.common.service.PlatformTokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
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

    private final MessageSource messageSource;

    private final MetaVideoService metaVideoService;

    private final ObjectMapper objectMapper;

    private final PlatformTokenService platformTokenService;

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
            throw new InvalidExcelFormatException("validation.default");
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
                throw new InvalidExcelFormatException("excel.error.header.missing");
            }

            OptionalInt invalidIndex = IntStream.range(0, expectedHeaders.size())
                    .filter(i -> {
                        Cell cell = headerRow.getCell(i);
                        return cell == null || !expectedHeaders.get(i).equals(cell.getStringCellValue().trim());
                    })
                    .findFirst();

            if (invalidIndex.isPresent()) {
                int idx = invalidIndex.getAsInt();
                throw new InvalidExcelFormatException("excel.error.header.invalid", idx + 1, expectedHeaders.get(idx));
            }
        }
    }

    private ExcelResponse processMetaIdentifiers(@Valid ExcelRowDto excelRowDto) throws JsonProcessingException {
        String accessToken = platformTokenService.getToken(Platform.META);

        String accountIdResponse = httpClientHelper.get(
                META_URL
                        + "/v22.0/me/adaccounts?access_token="
                        + accessToken
                        + "&fields=name,id"
        );
        excelRowDto.setAdAccountIdList(parseIdList(accountIdResponse, excelRowDto.getAdAccountName()));
        if (excelRowDto.getAdAccountIdList() == null || excelRowDto.getAdAccountIdList().size() != 1) {
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
        return objectMapper.readTree(json).get("id").asText();
    }

    private List<String> parseIdList(String json, String targetName) throws JsonProcessingException {
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


    public AdResponse publishAd(AdRequest adRequest, List<MultipartFile> files) {
        String accessToken = platformTokenService.getToken(Platform.META);
        List<CompletableFuture<UploadResult>> futureResults = new ArrayList<>();
        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (Objects.requireNonNull(contentType).startsWith("image/")) {
                futureResults.add(uploadImage(adRequest.getAdAccountId(), file));
            } else if (contentType.startsWith("video/")) {
                futureResults.add(metaVideoService.uploadVideo(adRequest.getAdAccountId(), file, accessToken));
            }
        }
        List<UploadResult> uploadResults = futureResults.stream()
                .map(CompletableFuture::join)
                .toList();
        return AdResponse.builder()
                .uploadResults(uploadResults)
                .build();
    }

    @Async
    public CompletableFuture<UploadResult> uploadImage(String adAccountId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String accessToken = platformTokenService.getToken(Platform.META);
        try {
            byte[] fileBytes = file.getBytes();
            String encoded = Base64.getEncoder().encodeToString(fileBytes);
            String response = httpClientHelper.postFormIgnoreFail(META_URL
                            + "/v22.0/"
                            + adAccountId
                            + "/adimages",
                    form -> form
                            .with("access_token", accessToken)
                            .with("bytes", encoded)
            );
            JsonNode root = objectMapper.readTree(response);

            if (root.has("error")) {
                return CompletableFuture.completedFuture(
                        buildErrorResult(originalFilename, root.get("error"))
                );
            }
            String hash = root.path("images").path("bytes").path("hash").asText();
            boolean isSuccess = hash != null && !hash.isBlank();

            return CompletableFuture.completedFuture(
                    ImageUploadResult.builder()
                    .originalFilename(originalFilename)
                    .imageHash(hash)
                    .success(isSuccess)
                    .reason(
                            isSuccess ? null :
                                    messageSource.getMessage(
                                            "creative.file.hash.missing",
                                            null,
                                            LocaleContextHolder.getLocale()
                                    )
                    )
                    .build()
            );
        } catch (Exception ex) {
            String defaultMessage = messageSource.getMessage(
                    "validation.default",
                    null,
                    LocaleContextHolder.getLocale()
            );

            return CompletableFuture.completedFuture(
                    ImageUploadResult.builder()
                            .originalFilename(originalFilename)
                            .imageHash(null)
                            .success(false)
                            .reason(defaultMessage + ": " + ex.getMessage())
                            .build()
            );
        }
    }

    private ImageUploadResult buildErrorResult(String originalFilename, JsonNode errorNode) {
        String title = errorNode.path("error_user_title").asText(null);
        String msg = errorNode.path("error_user_msg").asText(null);
        String fallback = errorNode.path("message").asText(
                messageSource.getMessage(
                        "validation.default",
                        null,
                        LocaleContextHolder.getLocale()
                )
        );
        return ImageUploadResult.builder()
                .originalFilename(originalFilename)
                .imageHash(null)
                .success(false)
                .reason(title != null ? title + ": " + msg : fallback)
                .build();
    }

    public List<MetaAccountResponse> getAccounts() {
        String accessToken = platformTokenService.getToken(Platform.META);
        String response = httpClientHelper.get(
                META_URL
                + "/v22.0/me/accounts1"
                + "?access_token="
                + accessToken
                + "&fields=name,link,username,emails,website,phone,about,picture"
                + "&limit=1000"
        );
        try {
            MetaAccountListResponse accountList = objectMapper.readValue(response, MetaAccountListResponse.class);
            return accountList.getData();
        } catch (JsonProcessingException e) {
            throw new HttpClientException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "meta.api.error.generic");
        }
    }
    public void validateToken() {
        String accessToken = platformTokenService.getToken(Platform.META);

        healthCheck(accessToken);
    }

    private void healthCheck(String accessToken) {
        String url = META_URL
                + "/v22.0/me?access_token="
                + accessToken
                ;
        httpClientHelper.get(url);
    }

    public void insertToken(String accessToken) {
        platformTokenService.saveOrUpdateToken(Platform.META, accessToken);
    }
}

