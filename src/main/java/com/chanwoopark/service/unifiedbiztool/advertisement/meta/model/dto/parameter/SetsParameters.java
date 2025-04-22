package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.parameter;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.excel.ExcelRowDto;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.function.Consumer;

@Builder
@Getter
public class SetsParameters {

    private String name;

    private MetaOptimizationGoal optimizationGoal;

    private MetaBillingEvent billingEvent;

    private Long bidAmount;

    private Long dailyBudget;

    private String campaignId;

    private Targeting targeting;

    private MetaAdStatus status;

    private String accessToken;

    @Builder
    @Getter
    public static class Targeting {
        @JsonProperty("geo_locations")
        private GeoLocations geoLocations;

        @Getter
        @Builder
        public static class GeoLocations {
            private List<String> countries;
        }
    }

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
                    .with("daily_budget", String.valueOf(param.getDailyBudget()))
                    .with("campaign_id", param.getCampaignId())
                    .with("status", param.getStatus().name())
                    .with("access_token", param.getAccessToken());
                try {
                    String targetingJson = objectMapper.writeValueAsString(param.getTargeting());
                    form.with("targeting", targetingJson);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Failed to serialize targeting", e);
            }
        };
    }

    public static SetsParameters fromExcel(ExcelRowDto excelRowDto, String accessToken) {
        return SetsParameters.builder()
                .name(excelRowDto.getSetName())
                .optimizationGoal(MetaOptimizationGoal.REACH)
                .billingEvent(MetaBillingEvent.IMPRESSIONS)
                .bidAmount(1500L)
                .dailyBudget(excelRowDto.getBudget())
                .campaignId(excelRowDto.getFirstCampaignId())
                .status(MetaAdStatus.PAUSED)
                .accessToken(accessToken)
                .targeting(
                        SetsParameters.Targeting.builder()
                                .geoLocations(
                                        SetsParameters.Targeting.GeoLocations.builder()
                                                .countries(List.of("KR"))
                                                .build()
                                )
                                .build()
                )
                .build();
    }
}
