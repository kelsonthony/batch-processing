package com.kelsonthony.batchprocessing.controller;

import com.kelsonthony.batchprocessing.exception.ApiUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiUnavailableException.class)
    public ResponseEntity<String> handleApiUnavailableException(ApiUnavailableException ex) {
        return new ResponseEntity<>("A API de destino não está acessível para o envio do payload.", HttpStatus.SERVICE_UNAVAILABLE);
    }
}