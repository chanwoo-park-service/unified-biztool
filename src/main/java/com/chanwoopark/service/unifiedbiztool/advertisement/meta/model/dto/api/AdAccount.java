package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.dto.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class AdAccount {
    private String id;
    private String name;
    @JsonProperty("business_name")
    private String businessName;
    @JsonProperty("account_status")
    private String accountStatus;
}
