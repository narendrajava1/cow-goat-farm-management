package com.cgfms.animal.exception;

public class DuplicateTagException extends RuntimeException {
    public DuplicateTagException(String tagNumber) {
        super("Animal with tag number " + tagNumber + " already exists");
    }
}