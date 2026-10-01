package com.platter.exception;

public class GoogleMapsException extends RuntimeException {
    public GoogleMapsException(String message) { super(message); }
    public GoogleMapsException(String message, Throwable cause) { super(message, cause); }
}
