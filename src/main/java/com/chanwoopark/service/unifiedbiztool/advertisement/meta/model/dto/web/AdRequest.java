package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.web;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCampaignType;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCreativeFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class AdRequest {

    @NotNull(message = "{adRequest.index.notnull}")
    private Long index;
    private MetaCampaignType metaCampaignType;
    @NotNull(message = "{adRequest.adAccountName.notnull}")
    private String adAccountName;
    @NotNull(message = "{adRequest.adAccountId.notnull}")
    private String adAccountId;
    @NotNull(message = "{adRequest.campaignName.notnull}")
    private String campaignName;
    @NotNull(message = "{adRequest.campaignId.notnull}")
    private String campaignId;
    @NotNull(message = "{adRequest.budget.notnull}")
    private BigDecimal budget;
    @NotNull(message = "{adRequest.setName.notnull}")
    private String setName;
    @NotNull(message = "{adRequest.setId.notnull}")
    private String setId;
    @NotNull(message = "{adRequest.adMaterialName.notnull}")
    private String adMaterialName;
    private String shortUrl;
    private String displayUrl;
    private String uploadPage;
    private String memo;
    @NotNull(message = "{adRequest.code.notnull}")
    private String code;
    private String shortUrlFlag;
    @NotNull(message = "{adRequest.landingUrl.notnull}")
    private String landingUrl;
    private String cafe24Url;
    @NotNull(message = "{adRequest.metaCreativeFormat.notnull}")
    private MetaCreativeFormat metaCreativeFormat;
    @NotNull(message = "{adRequest.pageId.notnull}")
    private String pageId;
    @NotNull(message = "{adRequest.pixelId.notnull}")
    private String pixelId;
}
