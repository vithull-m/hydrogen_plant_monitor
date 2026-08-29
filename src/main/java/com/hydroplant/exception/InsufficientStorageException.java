package com.hydroplant.exception;

/**
 * Thrown when a storage tank operation would exceed capacity
 * or when there isn't enough hydrogen stored to fulfill a withdrawal.
 */
public class InsufficientStorageException extends Exception {

    public InsufficientStorageException(String message) {
        super(message);
    }

    public InsufficientStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
