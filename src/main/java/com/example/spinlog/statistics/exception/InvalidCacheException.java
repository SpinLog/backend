package com.example.spinlog.statistics.exception;

public class InvalidCacheException extends RuntimeException {
    public InvalidCacheException() {
        super();
    }

    public InvalidCacheException(String message) {
        super(message);
    }

    public InvalidCacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
