package com.example.demo.exception;

public class GeocodingException extends RuntimeException {
    public GeocodingException(String message) {
        super(message);
    }
}
