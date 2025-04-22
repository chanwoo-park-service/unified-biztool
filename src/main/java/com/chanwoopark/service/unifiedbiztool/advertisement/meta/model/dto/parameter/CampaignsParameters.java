package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.parameter;

import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.excel.ExcelRowDto;
import com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums.*;
import lombok.Builder;
import lombok.Getter;
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

    public static CampaignsParameters fromExcel(ExcelRowDto excelRowDto, String accessToken) {
        return CampaignsParameters.builder()
                .name(excelRowDto.getCampaignName())
                .objective(MetaCampaignObjective.OUTCOME_SALES)
                .status(MetaAdStatus.PAUSED)
                .accessToken(accessToken)
                .specialAdCategory(MetaSpecialAdCategory.NONE)
                .build();
    }

    public static Consumer<BodyInserters.FormInserter<String>> toForm(CampaignsParameters param) {
        return form -> {
            form.with("name", param.getName())
                    .with("objective", param.getObjective().name())
                    .with("status", param.getStatus().name())
                    .with("access_token", param.getAccessToken())
                    .with("special_ad_categories", param.getSpecialAdCategory().name());
        };
    }
}
