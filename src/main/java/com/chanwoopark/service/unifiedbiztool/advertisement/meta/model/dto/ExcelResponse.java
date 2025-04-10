package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.MetaCampaignType;
import lombok.Builder;

import java.util.List;

@Builder
public record ExcelResponse(
        MetaCampaignType metaCampaignType,
        String advertiseAccountName,
        List<String> advertiseAccountIdList,
        String campaignName,
        List<String> campaignIdList,
        String budget,
        String setName,
        List<String> setIdList,
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

    public static ExcelResponse of(ExcelRowDto excelRowDto) {
        return ExcelResponse.builder()
                .metaCampaignType(excelRowDto.getMetaCampaignType())
                .advertiseAccountName(excelRowDto.getAdvertiseAccountName())
                .advertiseAccountIdList(excelRowDto.getAdvertiseAccountIdList())
                .campaignName(excelRowDto.getCampaignName())
                .campaignIdList(excelRowDto.getCampaignIdList())
                .budget(excelRowDto.getBudget())
                .setName(excelRowDto.getSetName())
                .setIdList(excelRowDto.getSetIdList())
                .creativeName(excelRowDto.getCreativeName())
                .shortUrl(excelRowDto.getShortUrl())
                .displayUrl(excelRowDto.getDisplayUrl())
                .uploadPage(excelRowDto.getUploadPage())
                .memo(excelRowDto.getMemo())
                .code(excelRowDto.getCode())
                .shortUrlFlag(excelRowDto.getShortUrlFlag())
                .landingUrl(excelRowDto.getLandingUrl())
                .cafe24Url(excelRowDto.getCafe24Url())
                .build();
    }

}
