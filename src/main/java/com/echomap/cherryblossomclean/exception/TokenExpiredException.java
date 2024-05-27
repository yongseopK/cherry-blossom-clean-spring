package com.echomap.cherryblossomclean.exception;

import io.jsonwebtoken.ExpiredJwtException;

public class TokenExpiredException extends RuntimeException{

    public TokenExpiredException(String message) { super(message); };

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
