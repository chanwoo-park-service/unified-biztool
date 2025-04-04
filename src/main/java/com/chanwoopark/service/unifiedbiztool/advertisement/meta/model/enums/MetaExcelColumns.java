package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Getter
public enum MetaExcelColumns {
    CAMPAIGN_TYPE("캠페인 형태"),
    CAMPAIGN_NAME("캠페인명 [제품명] 캠페인유형 캠페인 (A) - 날짜"),
    BUDGET("예산"),
    SET_NAME("세트명  A-B. 소구/(타겟) - 날짜"),
    MATERIAL_NAME("소재명 (광고코드, 세트명 입력 시 자동생성)"),
    SHORT_URL("(입력X) 생성된 단축링크 URL"),
    DISPLAY_URL("표시 링크"),
    UPLOAD_PAGE("업로드 페이지명"),
    MEMO("기타 전달사항"),
    CODE("광고코드"),
    SHORT_URL_FLAG("단축링크 생성여부 (x)"),
    LANDING_URL("랜딩 URL (한글이 들어간 주소명은 사용 금지)"),
    CAFE24_URL("(입력X) 생성된 cafe24 URL");

    private final String header;


    public static List<String> headers() {
        return Arrays.stream(values()).map(MetaExcelColumns::getHeader).toList();
    }

}
