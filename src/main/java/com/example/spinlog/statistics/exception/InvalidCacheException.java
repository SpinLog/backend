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

    public InvalidCacheException(Throwable cause) {
        super(cause);
    }

    protected InvalidCacheException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
