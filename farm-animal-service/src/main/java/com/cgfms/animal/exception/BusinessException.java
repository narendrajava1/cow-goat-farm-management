package com.cgfms.animal.exception;

/**
 * Thrown when a business rule is violated (e.g. deleting a herd that still has animals).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
