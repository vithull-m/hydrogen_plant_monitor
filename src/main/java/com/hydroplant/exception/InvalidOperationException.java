package com.hydroplant.exception;

/**
 * Thrown when an operation is requested that is not valid given the
 * current state of the plant (e.g. running a unit that is under maintenance).
 */
public class InvalidOperationException extends Exception {

    public InvalidOperationException(String message) {
        super(message);
    }
}
