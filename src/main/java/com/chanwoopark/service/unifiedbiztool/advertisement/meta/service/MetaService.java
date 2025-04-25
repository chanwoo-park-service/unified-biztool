package com.chanwoopark.service.unifiedbiztool.advertisement.meta.service;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception.HttpClientException;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception.InvalidExcelFormatException;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.*;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.Set;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.excel.ExcelRowDto;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.parameter.CampaignsParameters;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.parameter.SetsParameters;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.web.AdRequest;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.web.AdResponse;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.web.ExcelResponse;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.*;
import com.chanwoopark.service.unifiedbiztool.api.service.ApiCacheService;
import com.chanwoopark.service.unifiedbiztool.common.http.HttpClientHelper;
import com.chanwoopark.service.unifiedbiztool.common.model.enums.Platform;
import com.chanwoopark.service.unifiedbiztool.common.service.PlatformTokenService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@RequiredArgsConstructor
@Service
public class MetaService {

    private final HttpClientHelper httpClientHelper;

    private final MetaUploadService metaUploadService;

    private final ObjectMapper objectMapper;

    private final PlatformTokenService platformTokenService;

    private final ApiCacheService apiCacheService;

    private final String META_URL = "https://graph.facebook.com";

    public List<ExcelResponse> processExcel(MultipartFile file) {
        List<CompletableFuture<ExcelResponse>> futures = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int dataStartRowIndex = 2;

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
            Row headerRow = sheet.getRow(1);

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
        String accountsUrl = META_URL + "/v22.0/me/adaccounts?access_token="
                + accessToken
                + "&fields=id,name,business_name,account_status"
                + "&limit=1000";
        List<AdAccount> accountList = getResource(
                accountsUrl,
                AdAccount.class,
                AdAccount::getName,
                excelRowDto.getAdAccountName(),
                null
        );
        excelRowDto.setAdAccountList(accountList);
        if (accountList.size() != 1) {
            excelRowDto.setErrorMessage(
                    "해당 이름을 가진 광고 계정이 "
                            + accountList.size()
                            + "개입니다. 광고 계정은 반드시 1개로 특정되어야만 합니다.");
            return ExcelResponse.of(excelRowDto);
        }
        String accountId = excelRowDto.getFirstAccountId();
        excelRowDto.setAccountResolved(excelRowDto.getAdAccountList() != null && excelRowDto.getAdAccountList().size() == 1);

        List<Pixel> pixelList;

        try {
            pixelList = getPixels(
                    accountId,
                    accessToken
            );
        } catch (Exception ex) {
            excelRowDto.setErrorMessage(
                    "진행중 오류가 발생했습니다. 예외명 : "
                            + ex.getClass().getSimpleName()
                            + ", 메세지 : " + ex.getMessage()
            );
            return ExcelResponse.of(excelRowDto);
        }
        excelRowDto.setPixelList(pixelList);

        List<Campaign> campaignList;
        try {
            campaignList = getCampaigns(excelRowDto, accountId, accessToken);
        } catch (Exception ex) {
            excelRowDto.setErrorMessage(
                    ("진행중 오류가 발생했습니다. 예외명 : "
                            + ex.getClass().getSimpleName()
                            + ", 메세지 : " + ex.getMessage())
            );
            return ExcelResponse.of(excelRowDto);
        }
        excelRowDto.setCampaignList(campaignList);
        excelRowDto.setCampaignResolved(excelRowDto.getCampaignList() != null && excelRowDto.getCampaignList().size() == 1);

        List<Set> setList;
        try {
            setList = getSets(excelRowDto, accountId, accessToken);
        } catch (Exception ex) {
            excelRowDto.setErrorMessage(
                    "진행중 오류가 발생했습니다. 예외명 : "
                            + ex.getClass().getSimpleName()
                            + ", 메세지 : " + ex.getMessage()
            );
            return ExcelResponse.of(excelRowDto);
        }
        excelRowDto.setSetList(setList);
        excelRowDto.setSetResolved(excelRowDto.getSetList() != null && excelRowDto.getSetList().size() == 1);

        String pageUrl = META_URL
                + "/v22.0/me/accounts?access_token="
                + accessToken
                + "&fields=link,picture,name,id"
                + "&limit=1000";

        List<Page> pageList;

        try {
            pageList = getResource(
                    pageUrl,
                    Page.class,
                    Page::getId,
                    excelRowDto.getUploadPage(),
                    accountId
            );
        } catch (Exception ex) {
            excelRowDto.setErrorMessage(
                    "진행중 오류가 발생했습니다. 예외명 : "
                            + ex.getClass().getSimpleName()
                            + ", 메세지 : " + ex.getMessage()
            );
            return ExcelResponse.of(excelRowDto);
        }

        excelRowDto.setUploadPageList(pageList);
        excelRowDto.setPageResolved(excelRowDto.getUploadPageList() != null && excelRowDto.getUploadPageList().size() == 1);
        return ExcelResponse.of(excelRowDto);
    }

    private <T> List<T> getResource(String getUrl, Class<T> itemType, Function<T, String> nameExtractor, String nameToFind, String accountId) {
        String lockIdentifier = accountId + ":" + itemType.getSimpleName();
        Predicate<T> nameFilter = getNameFilter(nameExtractor, nameToFind);
        return apiCacheService.getOrFetch(
                lockIdentifier,
                () -> retrieveItems(
                        getUrl,
                        itemType
                ),
                nameFilter,
                itemType
        );
    }

    private static <T> Predicate<T> getNameFilter(Function<T, String> nameExtractor, String nameToFind) {
        return item -> {
            String name = nameExtractor.apply(item);
            return name != null && name.trim().equals(nameToFind);
        };
    }

    private List<Pixel> getPixels(String accountId, String accessToken) {
        String pixelUrl = META_URL
                + "/v22.0/"
                + Objects.requireNonNull(accountId)
                + "/adspixels";

        String pixelQueryParameters = "&fields=id,name,creation_time"
                + "&limit=1000";

        return getResource(
                accountId,
                pixelUrl,
                pixelQueryParameters,
                accessToken,
                Pixel.class
        );
    }

    private List<Campaign> getCampaigns(ExcelRowDto excelRowDto, String accountId, String accessToken) {
        String campaignUrl = META_URL
                + "/v22.0/"
                + Objects.requireNonNull(accountId)
                + "/campaigns";

        String campaignQueryParameters = "&fields=id,name,objective,status,special_ad_categories,start_time,daily_budget"
                + "&limit=1000";

        return getOrCreateResource(
                accountId,
                campaignUrl,
                campaignQueryParameters,
                accessToken,
                excelRowDto.getCampaignName(),
                Campaign.class,
                Campaign::getName,
                () -> CampaignsParameters.toForm(
                        CampaignsParameters.fromExcel(excelRowDto, accessToken)
                )
        );
    }

    private List<Set> getSets(ExcelRowDto excelRowDto, String accountId, String accessToken) {
        String setUrl = META_URL
                + "/v22.0/"
                + Objects.requireNonNull(accountId)
                + "/adsets";

        String setQueryParameters = "&fields=id,name,optimization_goal,billing_event,bid_amount,campaign_id,targeting,status,start_time,promoted_object"
                + "&limit=1000";

        return getOrCreateResource(
                accountId,
                setUrl,
                setQueryParameters,
                accessToken,
                excelRowDto.getSetName(),
                Set.class,
                Set::getName,
                () -> SetsParameters.toForm(
                        SetsParameters.fromExcel(
                                excelRowDto,
                                accessToken
                        ),
                        objectMapper
                )
        );
    }

    private <T> List<T> getOrCreateResource(
            String accountId,
            String baseUrl,
            String queryParameters,
            String accessToken,
            String nameToFind,
            Class<T> itemType,
            Function<T, String> nameExtractor,
            Supplier<Consumer<BodyInserters.FormInserter<String>>> formBuilder
    ) {
        String identifier = accountId + ":" + itemType.getSimpleName();
        String getUrl = baseUrl + "?access_token=" + accessToken
                + queryParameters;

        return apiCacheService.getOrCreateSync(
                identifier,
                Platform.META,
                () -> retrieveItems(
                        getUrl,
                        itemType
                ),
                getNameFilter(nameExtractor, nameToFind),
                () -> {
                    String response = httpClientHelper.postForm(baseUrl, formBuilder.get());
                    try {
                        String id = objectMapper.readTree(response).get("id").asText();
                        return apiCacheService.getAndForceCaching(
                                identifier,
                                () -> httpClientHelper.get(
                                        META_URL
                                                + "/v22.0/"
                                                + id
                                                + "?access_token=" + accessToken
                                                + queryParameters
                                ),
                                itemType
                        );
                    } catch (JsonProcessingException | ExecutionException | InterruptedException e) {
                        log.error("Failed to parse response as {}: {}", itemType.getSimpleName(), response, e);
                        throw new RuntimeException(e);
                    }
                },
                itemType
        );
    }

    private <T> List<T> getResource(
            String accountId,
            String baseUrl,
            String queryParameters,
            String accessToken,
            Class<T> itemType
    ) {
        String identifier = accountId + ":" + itemType.getSimpleName();
        String getUrl = baseUrl + "?access_token=" + accessToken
                + queryParameters;

        return apiCacheService.getOrFetch(
                identifier,
                () -> retrieveItems(
                        getUrl,
                        itemType
                ),
                t -> true,
                itemType
        );
    }

    private <T> List<T> retrieveItems(String getUrl, Class<T> itemType) {
        return streamAll(
                getUrl,
                itemType
        )
                .toList();
    }

    public <T> Stream<T> streamAll(String baseUrl, Class<T> itemType) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(new Iterator<>() {
                    final Queue<T> buffer = new LinkedList<>();
                    String nextUrl = baseUrl;

                    @Override
                    public boolean hasNext() {
                        fetchNextIfEmpty();
                        return !buffer.isEmpty();
                    }

                    @Override
                    public T next() {
                        fetchNextIfEmpty();
                        return buffer.poll();
                    }

                    private void fetchNextIfEmpty() {
                        if (!buffer.isEmpty() || nextUrl == null) return;
                        MetaListResponse<T> response = fetchPage(nextUrl, itemType);
                        buffer.addAll(response.getData());
                        nextUrl = Optional.ofNullable(response.getPaging()).map(MetaListResponse.Paging::getNext).orElse(null);
                    }
                }, Spliterator.ORDERED), false
        );
    }

    private <T> MetaListResponse<T> fetchPage(String url, Class<T> itemType) {
        try {
            String responseBody = httpClientHelper.get(url);

            JavaType type = objectMapper.getTypeFactory()
                    .constructParametricType(MetaListResponse.class, itemType);
            return objectMapper.readValue(responseBody, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch Meta paginated response", e);
        }
    }

    public AdResponse publishAd(AdRequest adRequest, List<MultipartFile> files, List<MultipartFile> thumbnails) throws JsonProcessingException {
        String accessToken = platformTokenService.getToken(Platform.META);
        List<CompletableFuture<UploadResult>> futureResults = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String contentType = file.getContentType();
            if (contentType == null) continue;

            if (contentType.startsWith("image/")) {
                futureResults.add(metaUploadService.uploadImage(adRequest.getAdAccountId(), file, accessToken));
            } else if (contentType.startsWith("video/")) {
                MultipartFile thumbnail = (thumbnails != null && thumbnails.size() > i) ? thumbnails.get(i) : null;
                futureResults.add(metaUploadService.uploadVideo(adRequest.getAdAccountId(), file, accessToken, thumbnail));
            }
        }
        List<UploadResult> uploadResults = futureResults.stream()
                .map(CompletableFuture::join)
                .toList();

        String creativeId;

        try {
            creativeId = uploadCreative(
                    uploadResults,
                    adRequest,
                    accessToken
            );
        } catch (Exception ex) {
            return AdResponse.builder()
                    .index(adRequest.getIndex())
                    .uploadResults(uploadResults)
                    .errorMessage(
                            "진행중 오류가 발생했습니다. 예외명 : "
                                    + ex.getClass().getSimpleName()
                                    + ", 메세지 : " + ex.getMessage()
                    )
                    .build();
        }

        String setUpdateUrl = META_URL + "/v22.0/" + adRequest.getSetId();

        try {
            httpClientHelper.postFormIgnoreFail(
                    setUpdateUrl,
                    SetsParameters.toForm(
                            SetsParameters.fromAdRequest(adRequest, accessToken),
                            objectMapper
                    )
            );
        } catch (Exception ex) {
            return AdResponse.builder()
                    .index(adRequest.getIndex())
                    .uploadResults(uploadResults)
                    .errorMessage(
                            "진행중 오류가 발생했습니다. 예외명 : "
                            + ex.getClass().getSimpleName()
                            + ", 메세지 : " + ex.getMessage()
                    )
                    .build();
        }

        String adsUrl = META_URL
                + "/v22.0/"
                + adRequest.getAdAccountId()
                + "/ads";

        String adsResponse = httpClientHelper.postForm(
                adsUrl,
                form -> {
                    try {
                        form
                                .with("name", adRequest.getTitle())
                                .with("adset_id", adRequest.getSetId())
                                .with("creative", objectMapper.writeValueAsString(Map.of(
                                        "creative_id", creativeId
                                                )
                                        )
                                )
                                .with("status", String.valueOf(MetaAdStatus.PAUSED))
                                .with("access_token", accessToken);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
        JsonNode root = objectMapper.readTree(adsResponse);
        String adsId = root.path("id").asText();

        return AdResponse.builder()
                .index(adRequest.getIndex())
                .uploadResults(uploadResults)
                .adsId(adsId)
                .build();
    }

    private String uploadCreative(List<UploadResult> uploadResults, AdRequest adRequest, String accessToken) throws JsonProcessingException {
        MetaCreativeFormat metaCreativeFormat = adRequest.getMetaCreativeFormat();
        String creativeResponse = "";
        switch (metaCreativeFormat) {
            case DYNAMIC,COLLECTION:
                break;
            case SINGLE:
                creativeResponse = handleSingle(uploadResults, adRequest, accessToken);
                break;
            case SLIDESHOW:
                creativeResponse = handleSlideshow(uploadResults, adRequest, accessToken);
                break;
        }

        JsonNode root = objectMapper.readTree(creativeResponse);
        return root.path("id").asText();
    }

    private String handleSingle(List<UploadResult> uploadResults, AdRequest adRequest, String accessToken) {
        UploadResult uploadResult = uploadResults.get(0);
        String url = META_URL
                + "v22.0/"
                + adRequest.getAdAccountId()
                    + "/adcreatives";

            return httpClientHelper.postForm(url, form -> {
                form
                        .with("name", adRequest.getAdMaterialName())
                        .with("access_token", accessToken);

                if (uploadResult instanceof VideoUploadResult video) {
                    ImageUploadResult imageUploadResult = (ImageUploadResult) video.getThumbnailResult();
                    try {
                        form.with("object_story_spec", objectMapper.writeValueAsString(Map.of(
                                "page_id", adRequest.getPageId(),
                                "video_data", Map.of(
                                        "video_id", video.getVideoId(),
                                        "image_url", imageUploadResult.getUrl(),
                                        "link", adRequest.getLandingUrl()
                                )
                        )));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                } else if (uploadResult instanceof ImageUploadResult image) {
                    try {
                        form.with("object_story_spec", objectMapper.writeValueAsString(Map.of(
                                "page_id", adRequest.getPageId(),
                                "link_data", Map.of(
                                        "image_hash", image.getImageHash(),
                                        "link", adRequest.getLandingUrl()
                                )
                        )));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
    }

    private String handleSlideshow(List<UploadResult> uploadResults, AdRequest adRequest, String accessToken) {
        String url = META_URL
                + "/v22.0/"
                + adRequest.getAdAccountId()
                + "/adcreatives";
        return httpClientHelper.postForm(url, form -> {
            form.with("name", adRequest.getAdMaterialName())
                    .with("access_token", accessToken);
            List<Map<String, Object>> childAttachments = new ArrayList<>();
            for (UploadResult result : uploadResults) {
                Map<String, Object> attachment = getAttachment(adRequest, (ImageUploadResult) result);
                childAttachments.add(attachment);
            }

            Map<String, Object> objectStorySpec = new HashMap<>();

            Map<String, Object> linkData = new HashMap<>();
            linkData.put("child_attachments", childAttachments);
            linkData.put("link", adRequest.getLandingUrl());

            objectStorySpec.put("link_data", linkData);
            objectStorySpec.put("page_id", adRequest.getPageId());

            try {
                form.with("object_story_spec", objectMapper.writeValueAsString(objectStorySpec));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        });
    }

    private static Map<String, Object> getAttachment(AdRequest adRequest, ImageUploadResult result) {
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("description", adRequest.getDescription());

        if (result.getImageHash() != null) {
            attachment.put("image_hash", result.getImageHash());
        }

        attachment.put("link", adRequest.getLandingUrl());
        attachment.put("name", adRequest.getDisplayUrl());
        return attachment;
    }

    public List<MetaAccountResponse> getAccounts() {
        String accessToken = platformTokenService.getToken(Platform.META);
        String response = httpClientHelper.get(
                META_URL
                + "/v22.0/me/accounts"
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

