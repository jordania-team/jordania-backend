package com.rodrigoborges.pocapi.auth;

public enum AuthProvider {
    APPLE,
    GOOGLE;

    public static AuthProvider from(String value) {
        return AuthProvider.valueOf(value.trim().toUpperCase());
    }
}
