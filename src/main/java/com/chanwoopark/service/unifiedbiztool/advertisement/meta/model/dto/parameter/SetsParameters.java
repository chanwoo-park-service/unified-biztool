package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.parameter;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.PromotedObject;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.Targeting;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.excel.ExcelRowDto;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.web.AdRequest;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.*;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.utils.MetaParameterParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

@Builder
@Getter
public class SetsParameters {

    private String name;

    private MetaOptimizationGoal optimizationGoal;

    private MetaBillingEvent billingEvent;

    private Long bidAmount;

    @Setter
    private Long dailyBudget;

    private String campaignId;

    @Setter
    @Getter
    private Targeting targeting;

    private MetaAdStatus status;

    private String accessToken;

    private String startTime;

    @Setter
    private PromotedObject promotedObject;



    public static Consumer<BodyInserters.FormInserter<String>> toForm(SetsParameters param, ObjectMapper objectMapper) {
        return form -> {
            form.with("name", param.getName())
                    .with("optimization_goal", param.getOptimizationGoal().name())
                    .with("billing_event", param.getBillingEvent().name())
                    .with("bid_amount", String.valueOf(param.getBidAmount()))
                    .with("campaign_id", param.getCampaignId())
                    .with("status", param.getStatus().name())
                    .with("access_token", param.getAccessToken())
                    .with("start_time", param.getStartTime());

            if (param.getDailyBudget() != null) {
                form.with("daily_budget", String.valueOf(param.getDailyBudget()));
            }

            if (param.getPromotedObject() != null) {
                try {
                    String promotedObjectJson = objectMapper.writeValueAsString(param.getPromotedObject());
                    form.with("promoted_object", promotedObjectJson);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize targeting", e);
                }
            }

            if (param.getTargeting() != null) {
                try {
                    String targetingJson = objectMapper.writeValueAsString(param.getTargeting());
                    form.with("targeting", targetingJson);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize targeting", e);
                }
            }

        };
    }

    public static SetsParameters fromExcel(ExcelRowDto excelRowDto, String accessToken) {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(excelRowDto.getStartDate(), excelRowDto.getStartTime(), ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        String formattedStartTime = zonedDateTime.format(formatter);
        SetsParameters parameters = SetsParameters.builder()
                .name(excelRowDto.getSetName())
                .optimizationGoal(MetaOptimizationGoal.LINK_CLICKS)
                .billingEvent(MetaBillingEvent.LINK_CLICKS)
                .bidAmount(1500L)
                .campaignId(excelRowDto.getFirstCampaignId())
                .status(MetaAdStatus.PAUSED)
                .accessToken(accessToken)
                .startTime(formattedStartTime)
                .targeting(
                        Targeting.builder()
                                .geoLocations(
                                        Targeting.GeoLocations.builder()
                                                .countries(MetaParameterParser.getGeoLocation(excelRowDto.getLocation()))
                                                .build()
                                )
                                .genders(MetaParameterParser.getGenders(excelRowDto.getGender()))
                                .locales(MetaParameterParser.getLocales(excelRowDto.getLanguage()))
                                .build()
                )
                        .build();

        setAge(excelRowDto.getMinAge(), excelRowDto.getMaxAge(), parameters);

        if (excelRowDto.getMetaCampaignType() == MetaCampaignType.ABO) {
            parameters.setDailyBudget(excelRowDto.getBudget());
        }

        if (!excelRowDto.getPixelList().isEmpty()) {
            parameters.setPromotedObject(
                    PromotedObject.builder()
                            .pixelId(excelRowDto.getFirstPixelId())
                            .metaCustomEventType(MetaCustomEventType.PURCHASE)
                            .build()
            );
        }

        return parameters;
    }

    public static SetsParameters fromAdRequest(AdRequest adRequest, String accessToken) {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(adRequest.getStartDate(), adRequest.getStartTime(), ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        String formattedStartTime = zonedDateTime.format(formatter);
        SetsParameters parameters = SetsParameters.builder()
                .name(adRequest.getSetName())
                .optimizationGoal(MetaOptimizationGoal.LINK_CLICKS)
                .billingEvent(MetaBillingEvent.LINK_CLICKS)
                .bidAmount(1500L)
                .campaignId(adRequest.getCampaignId())
                .status(MetaAdStatus.PAUSED)
                .accessToken(accessToken)
                .startTime(formattedStartTime)
                .targeting(
                        Targeting.builder()
                                .geoLocations(
                                        Targeting.GeoLocations.builder()
                                                .countries(MetaParameterParser.getGeoLocation(adRequest.getLocation()))
                                                .build()
                                )
                                .genders(MetaParameterParser.getGenders(adRequest.getGender()))
                                .locales(MetaParameterParser.getLocales(adRequest.getLanguage()))
                                .build()
                )
                .build();
        setAge(adRequest.getMinAge(), adRequest.getMaxAge(), parameters);

        if (adRequest.getMetaCampaignType() == MetaCampaignType.ABO) {
            parameters.setDailyBudget(adRequest.getBudget());
        }

        parameters.setPromotedObject(
                PromotedObject.builder()
                        .pixelId(adRequest.getPixelId())
                        .metaCustomEventType(MetaCustomEventType.PURCHASE)
                        .build()
        );

        return parameters;
    }

    private static void setAge(Integer minAge, String maxAge, SetsParameters parameters) {
        if (parameters.getTargeting() == null) {
            parameters.setTargeting(Targeting.builder().build());
        }
        if (minAge != null && (isInteger(maxAge))) {
            parameters.getTargeting().setAgeMin(minAge);
            parameters.getTargeting().setAgeMax(maxAge);

        } else if (minAge != null && (maxAge != null && maxAge.equals("+"))) {
            parameters.getTargeting().setAgeMin(minAge);
        } else if (minAge != null) {
            parameters.getTargeting().setAgeMin(minAge);
        }
    }

    public static boolean isInteger(String str) {
        if (str == null || str.isBlank()) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
