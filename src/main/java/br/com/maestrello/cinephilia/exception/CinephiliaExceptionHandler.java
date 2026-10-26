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

  @ExceptionHandler({
    org.springframework.http.converter.HttpMessageNotReadableException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<Object> handleMalformedRequest(Exception ex) {
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Malformed request body or identifier");
  }

  public static final String TIMESTAMP = "timestamp";
  public static final String STATUS = "status";
  public static final String ERROR = "error";
  public static final String MESSAGE = "message";

  @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
  public ResponseEntity<Object> handleResponseStatusException(
      org.springframework.web.server.ResponseStatusException ex) {
    return buildErrorResponse(HttpStatus.valueOf(ex.getStatusCode().value()), ex.getReason());
  }

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
    return buildErrorResponse(HttpStatus.valueOf(ex.status()), "TMDB API error: " + ex.status());
  }

  private ResponseEntity<Object> buildErrorResponse(HttpStatus status, String message) {
    Map<String, Object> errorBody = new HashMap<>();
    errorBody.put(TIMESTAMP, LocalDateTime.now());
    errorBody.put(STATUS, status.value());
    errorBody.put(ERROR, status.getReasonPhrase());
    errorBody.put(MESSAGE, message);
    return new ResponseEntity<>(errorBody, status);
  }
}
