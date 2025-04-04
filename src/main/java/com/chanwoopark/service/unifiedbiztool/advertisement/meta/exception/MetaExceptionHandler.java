package com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception;

import com.chanwoopark.service.unifiedbiztool.common.model.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

@Slf4j
@RestControllerAdvice(basePackages = "com.chanwoopark.service.unifiedbiztool.advertisement.meta")
public class MetaExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest httpServletRequest) {
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ex.getMessage(),
                        httpServletRequest.getRequestURI()
                )
        );
    }

    @ExceptionHandler({IllegalArgumentException.class, MultipartException.class})
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest httpServletRequest) {
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        String.format("파일이 비어있거나 정상적이지 않습니다. (%s)", ex.getMessage()),
                        httpServletRequest.getRequestURI()
                )
        );
    }
}
