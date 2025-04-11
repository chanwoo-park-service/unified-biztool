package com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception;

import lombok.Getter;

@Getter
public class InvalidExcelFormatException extends RuntimeException {

    private final Object[] args;

    public InvalidExcelFormatException(String message) {
        super(message);
        this.args = null;
    }

    public InvalidExcelFormatException(String message, Object... args) {
        super(message);
        this.args = args;
    }

}
