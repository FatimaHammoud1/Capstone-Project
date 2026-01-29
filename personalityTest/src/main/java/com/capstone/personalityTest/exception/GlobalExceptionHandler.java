package com.capstone.personalityTest.exception;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


//Spring’s @RestControllerAdvice is designed to intercept exceptions globally.
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException e){
        return new ResponseEntity<>(e.getMessage() , HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<String> handleFound(EntityExistsException e){
        return new ResponseEntity<>(e.getMessage() , HttpStatus.FOUND);
    }
}
