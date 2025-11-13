package org.example.minispring.exception;

public class NoSuchBeanException extends RuntimeException {
    public NoSuchBeanException(String message) {
        super(message);
    }
}
