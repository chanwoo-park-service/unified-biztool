package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.parameter;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.excel.ExcelRowDto;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.function.Consumer;


@Getter
@Builder
public class CampaignsParameters {

    private String name;

    private MetaCampaignObjective objective;

    private MetaAdStatus status;

    private String accessToken;

    private MetaSpecialAdCategory specialAdCategory;

    @Setter
    private Long budget;

    public static CampaignsParameters fromExcel(ExcelRowDto excelRowDto, String accessToken) {
        CampaignsParameters parameters = CampaignsParameters.builder()
                .name(excelRowDto.getCampaignName())
                .objective(excelRowDto.getMetaCampaignObjective())
                .status(MetaAdStatus.PAUSED)
                .accessToken(accessToken)
                .specialAdCategory(MetaSpecialAdCategory.NONE)
                .build();

        if (excelRowDto.getMetaCampaignType() == MetaCampaignType.CBO || excelRowDto.getMetaCampaignType() == MetaCampaignType.ASC) {
            parameters.setBudget(excelRowDto.getBudget());
        }

        return parameters;
    }

    public static Consumer<BodyInserters.FormInserter<String>> toForm(CampaignsParameters param) {
        return form -> {
            form.with("name", param.getName())
                    .with("objective", param.getObjective().name())
                    .with("status", param.getStatus().name())
                    .with("access_token", param.getAccessToken())
                    .with("special_ad_categories", param.getSpecialAdCategory().name());
            if (param.getBudget() != null && param.getBudget() != 0L) {
                form.with("daily_budget", String.valueOf(param.getBudget()));
            }
        };
    }
}
