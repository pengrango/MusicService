package com.musify.app.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<String> resourceNotFound(WebClientResponseException ex) {
        if (ex.getStatusCode().is4xxClientError()) {
            logger.error("Client error", ex);
            return ResponseEntity.status(ex.getStatusCode()).body("Error in Client request.");
        } else {
            logger.error("Internal error", ex);
            return ResponseEntity.status(ex.getStatusCode()).body("Internal error.");
        }
    }
}
