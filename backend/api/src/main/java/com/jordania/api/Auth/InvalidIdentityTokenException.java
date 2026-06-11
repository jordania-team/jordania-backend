package com.rodrigoborges.pocapi.auth;

public class InvalidIdentityTokenException extends RuntimeException {

    public InvalidIdentityTokenException(String message) {
        super(message);
    }
}
