package com.swisscom.mycoolservice.exception;
/**
 * Exception thrown to indicate an attempt to create a duplicate bean.
 */
public class DuplicateBeanException extends RuntimeException {
    public DuplicateBeanException(String message) {
        super(message);
    }
}
