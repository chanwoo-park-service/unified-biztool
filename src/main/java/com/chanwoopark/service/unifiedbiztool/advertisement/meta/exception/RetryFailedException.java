package com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception;

import lombok.Getter;

public class RetryFailedException extends RuntimeException {

    private final EntityType entityType;
    private final int count;

    public RetryFailedException(EntityType entityType, int count) {
        super(entityType.getErrorMessage(count));
        this.entityType = entityType;
        this.count = count;
    }

    public RetryFailedException(String message, Throwable cause) {
        super(message, cause);
        this.entityType = null;
        this.count = 0;
    }

    @Getter
    public enum EntityType {
        AD_ACCOUNT("광고 계정"),
        PIXEL("픽셀"),
        CAMPAIGN("캠페인"),
        SET("세트"),
        PAGE("페이지");

        private final String name;

        EntityType(String name) {
            this.name = name;
        }

        /**
         * 엔티티 타입별 에러 메시지 생성
         *
         * @param count 조회된 엔티티 개수
         * @return 포맷팅된 에러 메시지
         */
        public String getErrorMessage(int count) {
            return name + "이(가) 하나로 특정되지 않았습니다. 조회된 " + name + "은(는) " + count + "개 입니다.";
        }
    }
}
