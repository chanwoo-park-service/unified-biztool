package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.web;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCampaignObjective;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCampaignType;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCreativeFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class AdRequest {
    @NotNull(message = "{adRequest.index.notnull}")
    private Long index;

    @NotNull(message = "{adRequest.metaCampaignObjective.notnull}")
    private MetaCampaignObjective metaCampaignObjective;

    @NotNull(message = "{adRequest.metaCampaignType.notnull}")
    private MetaCampaignType metaCampaignType;

    @NotNull(message = "{adRequest.adAccountName.notnull}")
    private String adAccountName;

    @NotNull(message = "{adRequest.adAccountId.notnull}")
    private String adAccountId;

    @NotNull(message = "{adRequest.pixelId.notnull}")
    private String pixelId;

    @NotNull(message = "{adRequest.campaignName.notnull}")
    private String campaignName;

    @NotNull(message = "{adRequest.campaignId.notnull}")
    private String campaignId;

    @NotNull(message = "{adRequest.budget.notnull}")
    private Long budget;

    @NotNull(message = "{adRequest.startDate.notnull}")
    private LocalDate startDate;

    @NotNull(message = "{adRequest.startTime.notnull}")
    private LocalTime startTime;

    @NotNull(message = "{adRequest.location.notnull}")
    private String location;

    @NotNull(message = "{adRequest.language.notnull}")
    private String language;

    @NotNull(message = "{adRequest.gender.notnull}")
    private String gender;

    @NotNull(message = "{adRequest.minAge.notnull}")
    private Integer minAge;

    @NotNull(message = "{adRequest.maxAge.notnull}")
    private String maxAge;

    @NotNull(message = "{adRequest.setName.notnull}")
    private String setName;

    @NotNull(message = "{adRequest.setId.notnull}")
    private String setId;

    @NotNull(message = "{adRequest.adMaterialName.notnull}")
    private String adMaterialName;

    @NotNull(message = "{adRequest.pageId.notnull}")
    private String pageId;

    @NotNull(message = "{adRequest.metaCreativeFormat.notnull}")
    private MetaCreativeFormat metaCreativeFormat;

    @NotNull(message = "{adRequest.landingUrl.notnull}")
    private String landingUrl;

    @NotNull(message = "{adRequest.displayUrl.notnull}")
    private String displayUrl;

    @NotNull(message = "{adRequest.defaultText.notnull}")
    private String defaultText;

    @NotNull(message = "{adRequest.title.notnull}")
    private String title;

    @NotNull(message = "{adRequest.description.notnull}")
    private String description;

    @NotNull(message = "{adRequest.otherRequests.notnull}")
    private String otherRequests;

    //unused;
    private String blank;
    private String adCode;
    private String isShortUrlCreate;
    private String shortUrl;
}
