package com.with.with.member;

public class ReviewCreationException extends RuntimeException {
    public ReviewCreationException(String message) {
        super(message);
    }

    public ReviewCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
