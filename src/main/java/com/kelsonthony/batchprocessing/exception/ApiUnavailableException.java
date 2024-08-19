package com.kelsonthony.batchprocessing.exception;

public class ApiUnavailableException extends RuntimeException {
    public ApiUnavailableException(String message) {
        super(message);
    }
}