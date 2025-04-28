package com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception;

import lombok.Getter;

@Getter
public class HttpClientException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public HttpClientException(int statusCode, String responseBody) {
        super("HTTP 요청 실패 (" + statusCode + ") : " + responseBody);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public HttpClientException(String responseBody) {
        super(responseBody);
        this.statusCode = 400;
        this.responseBody = responseBody;
    }
}
