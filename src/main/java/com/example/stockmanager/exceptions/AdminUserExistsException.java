package com.example.stockmanager.exceptions;

public class AdminUserExistsException extends RuntimeException {
    public AdminUserExistsException(String message) {
        super(message);
    }
}

