package com.fof.shared.api;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@RestControllerAdvice
public class GestionErreursGlobale {

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ErreurApi> notFound(EntityNotFoundException ex) {
    return construire(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ErreurApi> validation(ValidationException ex) {
    return construire(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErreurApi> argument(MethodArgumentNotValidException ex) {
    List<String> details = ex.getBindingResult().getAllErrors().stream()
        .map(err -> {
          if (err instanceof FieldError fe) {
            return fe.getField() + ": " + fe.getDefaultMessage();
          }
          return err.getDefaultMessage();
        })
        .toList();
    return construire(HttpStatus.BAD_REQUEST, "Validation échouée", details);
  }

  @ExceptionHandler({
      HttpMessageNotReadableException.class,
      MissingServletRequestPartException.class,
      MethodArgumentTypeMismatchException.class,
      HttpMediaTypeNotSupportedException.class
  })
  public ResponseEntity<ErreurApi> badRequest(Exception ex) {
    return construire(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErreurApi> methodNotAllowed(HttpRequestMethodNotSupportedException ex) {
    return construire(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), List.of());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErreurApi> generic(Exception ex) {
    log.error("Erreur non gérée", ex);
    return construire(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne", List.of());
  }

  private ResponseEntity<ErreurApi> construire(HttpStatus statut, String message, List<String> details) {
    return ResponseEntity.status(statut).body(new ErreurApi(Instant.now(), statut.value(), message, details));
  }
}

