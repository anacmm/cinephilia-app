package br.com.maestrello.cinephilia.exception;

import feign.FeignException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CinephiliaExceptionHandler {

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(FeignException.class)
  public ResponseEntity<Object> handleFeignError(FeignException ex) {
    return buildErrorResponse(
        HttpStatus.valueOf(ex.status()), "TMDB API error: " + ex.getMessage());
  }

  private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message) {
    Map<String, Object> errorBody = new HashMap<>();
    errorBody.put("timestamp", LocalDateTime.now());
    errorBody.put("status", status.value());
    errorBody.put("error", status.getReasonPhrase());
    errorBody.put("message", message);
    return new ResponseEntity<>(errorBody, status);
  }
}
