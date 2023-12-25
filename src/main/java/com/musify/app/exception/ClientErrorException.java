package com.musify.app.exception;

public class ClientErrorException extends RuntimeException{
    public ClientErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
