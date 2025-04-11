package com.chanwoopark.service.unifiedbiztool.advertisement.meta.exception;

import com.chanwoopark.service.unifiedbiztool.common.model.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice(basePackages = "com.chanwoopark.service.unifiedbiztool.advertisement.meta")
public class MetaExceptionHandler {

    private final MessageSource messageSource;

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
        String message = messageSource.getMessage(
                "validation.default",
                null,
                LocaleContextHolder.getLocale()
        ) + ": " + ex.getMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        message,
                        httpServletRequest.getRequestURI()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest httpServletRequest) {
        log.warn("Validation failed: {}", ex.getMessage());
        String errorMessages =  messageSource.getMessage(
                "validation.default",
                null,
                LocaleContextHolder.getLocale()
        ) + ": " + ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .distinct()
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse(
                        messageSource.getMessage(
                                "validation.default",
                                null,
                                LocaleContextHolder.getLocale()
                        )
                );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of(
                        HttpStatus.BAD_REQUEST,
                        errorMessages,
                        httpServletRequest.getRequestURI()
                )
        );
    }

    @ExceptionHandler(InvalidExcelFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidExcel(InvalidExcelFormatException ex, HttpServletRequest httpServletRequest) {
        String message = messageSource.getMessage(
                "validation.default",
                null,
                LocaleContextHolder.getLocale()
        ) + ": " + messageSource.getMessage(
                ex.getMessage(),
                ex.getArgs(),
                LocaleContextHolder.getLocale()
        );
        return ResponseEntity.badRequest().body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                message,
                httpServletRequest.getRequestURI()
        ));
    }
}
