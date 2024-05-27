package com.echomap.cherryblossomclean.exception;

public class TokenForgedException extends RuntimeException{

    public TokenForgedException(String message) {
        super(message);
    }

    public TokenForgedException(String message, Throwable cause) {
        super(message, cause);
    }
}
