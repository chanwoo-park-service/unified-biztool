package com.chanwoopark.service.unifiedbiztool.advertisement.meta.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Getter
public enum MetaExcelColumns {
    CAMPAIGN_GOAL("캠페인 목표"),
    ADVERTISE_ACCOUNT_NAME("광고계정명"),
    CAMPAIGN_TYPE("캠페인 형태"),
    CAMPAIGN_NAME("캠페인명"),
    BUDGET("예산"),
    AGE_RANGE("나이 범위(설정시 어드밴티지 + 타겟 활성화)"),
    START_DATE("시작 날짜"),
    START_TIME("시작 시간"),
    LOCATION("위치"),
    LANGUAGE("언어"),
    GENDER("성별"),
    MIN_AGE("최소 연령"),
    MAX_AGE("최대 연령"),
    SET_NAME("세트명"),
    MATERIAL_NAME("광고소재명"),
    UPLOAD_PAGE_NAME("업로드 페이지명"),
    FORMAT("형식"),
    LANDING_PAGE_URL("랜딩페이지 URL"),
    DISPLAY_LINK_URL("표시링크 URL"),
    DEFAULT_PHRASE("기본 문구"),
    TITLE("제목"),
    DESCRIPTION("설명"),
    MEMO("기타 요청사항"),
    BLANK(""),
    CODE("광고코드"),
    SHORT_URL_FLAG("단축링크 생성 여부( o,x)"),
    SHORT_URL("단축링크");

    private final String header;


    public static List<String> headers() {
        return Arrays.stream(values()).map(MetaExcelColumns::getHeader).toList();
    }

}
