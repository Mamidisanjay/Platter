package com.platter.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
