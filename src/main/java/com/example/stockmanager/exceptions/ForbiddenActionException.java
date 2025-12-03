package com.example.stockmanager.exceptions;

import org.springframework.security.access.AccessDeniedException;

public class ForbiddenActionException extends AccessDeniedException {
    public ForbiddenActionException(String msg) {
        super(msg);
    }
}
