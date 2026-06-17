package com.loan_org.notification_service.template_service.exception;

import com.loan_org.notification_service.shared.exception.AccessForbiddenException;
import com.loan_org.notification_service.shared.exception.template.TemplateAlreadyExists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(TemplateAlreadyExists.class)
    public ResponseEntity<String> handleTemplateAlreadyExists(TemplateAlreadyExists exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(AccessForbiddenException.class)
    public ResponseEntity<String> handleAccessForbiddenException(AccessForbiddenException exception) {
        log.warn(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception exception) {
        log.error(exception.getMessage(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

}
