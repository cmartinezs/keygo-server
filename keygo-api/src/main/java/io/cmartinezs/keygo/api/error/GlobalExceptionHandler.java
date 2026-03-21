package io.cmartinezs.keygo.api.error;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for all controllers.
 * Manejador global de excepciones para todos los controladores.
 *
 * <p>Handles exceptions uniformly across the application and returns standardized responses.
 * Maneja excepciones uniformemente en toda la aplicación y retorna respuestas estandarizadas.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles UnauthorizedException - returns 401 Unauthorized.
   * Maneja UnauthorizedException - retorna 401 Unauthorized.
   *
   * @param ex the exception / la excepción
   * @return ResponseEntity with error details / ResponseEntity con detalles del error
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<BaseResponse<Void>> handleUnauthorizedException(UnauthorizedException ex) {
    log.error("Unauthorized access attempt: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.AUTHENTICATION_REQUIRED))
        .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  /**
   * Handles NoResourceFoundException - returns 404 Not Found.
   * Maneja NoResourceFoundException - retorna 404 Not Found.
   *
   * @param ex the exception / la excepción
   * @return ResponseEntity with error details / ResponseEntity con detalles del error
   */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<BaseResponse<Void>> handleNoResourceFoundException(NoResourceFoundException ex) {
    log.error("Resource not found: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.RESOURCE_NOT_FOUND))
        .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  /**
   * Handles IllegalArgumentException - returns 400 Bad Request.
   * Maneja IllegalArgumentException - retorna 400 Bad Request.
   *
   * @param ex the exception / la excepción
   * @return ResponseEntity with error details / ResponseEntity con detalles del error
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<BaseResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.error("Invalid argument: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT))
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles @Valid validation errors - returns 400 Bad Request.
   * Maneja errores de validación @Valid - retorna 400 Bad Request.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
    log.error("Validation failed: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT))
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles TenantNotFoundException - returns 404 Not Found.
   * Maneja TenantNotFoundException - retorna 404 Not Found.
   */
  @ExceptionHandler(TenantNotFoundException.class)
  public ResponseEntity<BaseResponse<Void>> handleTenantNotFoundException(TenantNotFoundException ex) {
    log.error("Tenant not found: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.RESOURCE_NOT_FOUND))
        .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  /**
   * Handles TenantSuspendedException - returns 403 Forbidden.
   * Maneja TenantSuspendedException - retorna 403 Forbidden.
   */
  @ExceptionHandler(TenantSuspendedException.class)
  public ResponseEntity<BaseResponse<Void>> handleTenantSuspendedException(TenantSuspendedException ex) {
    log.error("Tenant suspended: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.BUSINESS_RULE_VIOLATION))
        .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  /**
   * Handles generic exceptions - returns 500 Internal Server Error.
   * Maneja excepciones genéricas - retorna 500 Internal Server Error.
   *
   * @param ex the exception / la excepción
   * @return ResponseEntity with error details / ResponseEntity con detalles del error
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<Void>> handleGenericException(Exception ex) {
    log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.OPERATION_FAILED))
        .build();

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}

