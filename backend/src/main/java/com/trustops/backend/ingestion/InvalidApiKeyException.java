package com.trustops.backend.ingestion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Invalid key expection
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED) //Spring turns this exception into HTTP 401
public class InvalidApiKeyException extends RuntimeException {
    public InvalidApiKeyException() {
        super("Invalid API key"); //safe message does not reveal which company/key exists
    }
}
