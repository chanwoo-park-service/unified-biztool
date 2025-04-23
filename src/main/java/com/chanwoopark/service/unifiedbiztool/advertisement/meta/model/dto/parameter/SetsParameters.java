package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.parameter;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api.Targeting;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.excel.ExcelRowDto;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.*;
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

    @Builder
    @Getter
    public static class PromotedObject {

        private String pixel_id;

        private MetaCustomEventType metaCustomEventType;

    }

    public static Consumer<BodyInserters.FormInserter<String>> toForm(SetsParameters param, ObjectMapper objectMapper) {
        return form -> {
                form.with("name", param.getName())
                    .with("optimization_goal", param.getOptimizationGoal().name())
                    .with("billing_event", param.getBillingEvent().name())
                    .with("bid_amount", String.valueOf(param.getBidAmount()))
                    .with("campaign_id", param.getCampaignId())
                    .with("status", param.getStatus().name())
                    .with("access_token", param.getAccessToken());

                if (param.getDailyBudget() != null) {
                    form.with("daily_budget", String.valueOf(param.getDailyBudget()));
                }

                try {
                    String targetingJson = objectMapper.writeValueAsString(param.getTargeting());
                    form.with("targeting", targetingJson);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize targeting", e);
            }
        };
    }

    public static SetsParameters fromExcel(ExcelRowDto excelRowDto, String accessToken) {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(excelRowDto.getStartDate(), excelRowDto.getStartTime(), ZoneId.of("Asia/Seoul"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

        SetsParameters parameters = SetsParameters.builder()
                .name(excelRowDto.getSetName())
                .optimizationGoal(MetaOptimizationGoal.LINK_CLICKS)
                .billingEvent(MetaBillingEvent.LINK_CLICKS)
                .bidAmount(1500L)
                .campaignId(excelRowDto.getFirstCampaignId())
                .status(MetaAdStatus.PAUSED)
                .accessToken(accessToken)
                .startTime(zonedDateTime.format(formatter))
                .targeting(
                        Targeting.builder()
                                .geoLocations(
                                        Targeting.GeoLocations.builder()
                                                .countries(excelRowDto.getGeoLocation())
                                                .build()
                                )
                                .genders(excelRowDto.getGenders())
                                .locales(excelRowDto.getLocales())
                                .build()
                )
                .build();


        if (excelRowDto.getMinAge() != null && (excelRowDto.getMaxAge() != null && isInteger(excelRowDto.getMaxAge()))) {
            parameters.getTargeting().setAgeMin(excelRowDto.getMinAge());
            parameters.getTargeting().setAgeMax(excelRowDto.getMaxAge());

        } else if (excelRowDto.getMinAge() != null && (excelRowDto.getMaxAge() != null && excelRowDto.getMaxAge().equals("+"))) {
            parameters.getTargeting().setAgeMin(excelRowDto.getMinAge());
        } else if (excelRowDto.getMinAge() != null) {
            parameters.getTargeting().setAgeMin(excelRowDto.getMinAge());
        }

        if (excelRowDto.getMetaCampaignType() == MetaCampaignType.ABO) {
            parameters.setDailyBudget(excelRowDto.getBudget());
        }
        return parameters;
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
