package org.f1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(OpenF1IngestException.class)
    public ResponseEntity<?> handleF1IngestException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse(e.getMessage(), HttpStatus.BAD_GATEWAY.value()));
    }
}
