package com.tlamatinisoft.mosentiliztli.exception;

public class RsvpValidationException extends RuntimeException {
    private final String errorCode;

    public RsvpValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
