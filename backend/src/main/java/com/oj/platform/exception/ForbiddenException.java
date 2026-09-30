package com.oj.platform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an authenticated user is recognized but is not allowed to perform the
 * requested action (e.g. an unverified user trying to host an assessment, or a user
 * trying to access another user's verification request). Distinct from
 * UnauthorizedException (401 - not authenticated at all).
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
