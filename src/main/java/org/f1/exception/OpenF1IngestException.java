package org.f1.exception;

public class OpenF1IngestException extends RuntimeException {

    public OpenF1IngestException(String message) {
        super(message);
    }

    public OpenF1IngestException(String message, Throwable cause) {
        super(message, cause);
    }
}
