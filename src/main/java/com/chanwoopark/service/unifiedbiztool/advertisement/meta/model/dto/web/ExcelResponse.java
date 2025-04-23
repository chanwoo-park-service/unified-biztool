package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.web;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.*;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.excel.ExcelRowDto;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCampaignObjective;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCampaignType;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCreativeFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class ExcelResponse {
    private MetaCampaignObjective metaCampaignObjective;
    private MetaCampaignType metaCampaignType;
    private String adAccountName;
    private List<AdAccount> adAccountList;
    private String campaignName;
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
    private List<Set> setList;
    private String adMaterialName;
    private String uploadPage;
    private List<Page> uploadPageList;
    private MetaCreativeFormat metaCreativeFormat;
    private String landingUrl;
    private String displayUrl;
    private String defaultText;
    private String title;
    private String description;
    private String otherRequests;
    private String blank;
    private String adCode;
    private String isShortUrlCreate;
    private String shortUrl;

    private boolean pageResolved;
    private boolean adAccountResolved;
    private boolean campaignResolved;
    private boolean setResolved;

    private String errorMessage;

    public static ExcelResponse of(ExcelRowDto dto) {
        return ExcelResponse.builder()
                .metaCampaignObjective(dto.getMetaCampaignObjective())
                .adAccountName(dto.getAdAccountName())
                .metaCampaignType(dto.getMetaCampaignType())
                .adAccountList(dto.getAdAccountList())
                .campaignName(dto.getCampaignName())
                .campaignList(dto.getCampaignList())
                .budget(dto.getBudget())
                .startDate(dto.getStartDate())
                .startTime(dto.getStartTime())
                .location(dto.getLocation())
                .language(dto.getLanguage())
                .gender(dto.getGender())
                .minAge(dto.getMinAge())
                .maxAge(dto.getMaxAge())
                .setName(dto.getSetName())
                .setList(dto.getSetList())
                .adMaterialName(dto.getAdMaterialName())
                .uploadPage(dto.getUploadPage())
                .uploadPageList(dto.getUploadPageList())
                .metaCreativeFormat(dto.getMetaCreativeFormat())
                .landingUrl(dto.getLandingUrl())
                .displayUrl(dto.getDisplayUrl())
                .defaultText(dto.getDefaultText())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .otherRequests(dto.getOtherRequests())
                .blank(dto.getBlank())
                .adCode(dto.getAdCode())
                .isShortUrlCreate(dto.getIsShortUrlCreate())
                .shortUrl(dto.getShortUrl())
                .pageResolved(dto.isPageResolved())
                .adAccountResolved(dto.isAccountResolved())
                .campaignResolved(dto.isCampaignResolved())
                .setResolved(dto.isSetResolved())
                .errorMessage(dto.getErrorMessage())
                .build();
    }

}
