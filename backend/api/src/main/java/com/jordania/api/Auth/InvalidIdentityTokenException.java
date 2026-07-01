package com.jordania.api.Auth;

public class InvalidIdentityTokenException extends RuntimeException {

    public InvalidIdentityTokenException(String message) {
        super(message);
    }
}
