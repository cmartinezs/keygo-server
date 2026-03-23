package io.cmartinezs.keygo.api.error;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.domain.auth.exception.AuthorizationCodeExpiredException;
import io.cmartinezs.keygo.domain.auth.exception.InvalidAuthorizationCodeException;
import io.cmartinezs.keygo.domain.auth.exception.InvalidPkceVerificationException;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.auth.exception.NoActiveSigningKeyException;
import io.cmartinezs.keygo.domain.auth.exception.RefreshTokenExpiredException;
import io.cmartinezs.keygo.domain.auth.exception.ScopeNotGrantedException;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.exception.InvalidRedirectUriException;
import io.cmartinezs.keygo.domain.clientapp.exception.UnsupportedGrantTypeException;
import io.cmartinezs.keygo.domain.membership.exception.InvalidRoleAssignmentException;
import io.cmartinezs.keygo.domain.membership.exception.MembershipInactiveException;
import io.cmartinezs.keygo.domain.membership.exception.MembershipNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.user.exception.DuplicateUserException;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
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
   * Handles ClientAppNotFoundException - returns 404 Not Found.
   * Maneja ClientAppNotFoundException - retorna 404 Not Found.
   */
  @ExceptionHandler(ClientAppNotFoundException.class)
  public ResponseEntity<BaseResponse<Void>> handleClientAppNotFoundException(ClientAppNotFoundException ex) {
    log.error("Client app not found: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.RESOURCE_NOT_FOUND))
        .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  /**
   * Handles InvalidRedirectUriException - returns 400 Bad Request.
   * Maneja InvalidRedirectUriException - retorna 400 Bad Request.
   */
  @ExceptionHandler(InvalidRedirectUriException.class)
  public ResponseEntity<BaseResponse<Void>> handleInvalidRedirectUriException(InvalidRedirectUriException ex) {
    log.error("Invalid redirect URI: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT))
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles UnsupportedGrantTypeException - returns 400 Bad Request.
   * Maneja UnsupportedGrantTypeException - retorna 400 Bad Request.
   */
  @ExceptionHandler(UnsupportedGrantTypeException.class)
  public ResponseEntity<BaseResponse<Void>> handleUnsupportedGrantTypeException(UnsupportedGrantTypeException ex) {
    log.error("Unsupported grant type: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT))
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles UserNotFoundException - returns 404 Not Found.
   * Maneja UserNotFoundException - retorna 404 Not Found.
   */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<BaseResponse<Void>> handleUserNotFoundException(UserNotFoundException ex) {
    log.error("User not found: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.RESOURCE_NOT_FOUND))
        .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  /**
   * Handles UserSuspendedException - returns 403 Forbidden.
   * Maneja UserSuspendedException - retorna 403 Forbidden.
   */
  @ExceptionHandler(UserSuspendedException.class)
  public ResponseEntity<BaseResponse<Void>> handleUserSuspendedException(UserSuspendedException ex) {
    log.error("User suspended: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.BUSINESS_RULE_VIOLATION))
        .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  /**
   * Handles DuplicateUserException - returns 409 Conflict.
   * Maneja DuplicateUserException - retorna 409 Conflict.
   */
  @ExceptionHandler(DuplicateUserException.class)
  public ResponseEntity<BaseResponse<Void>> handleDuplicateUserException(DuplicateUserException ex) {
    log.error("Duplicate user: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.DUPLICATE_RESOURCE))
        .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }

  /**
   * Handles InvalidCredentialsException - returns 401 Unauthorized.
   * Maneja InvalidCredentialsException - retorna 401 Unauthorized.
   */
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<BaseResponse<Void>> handleInvalidCredentialsException(InvalidCredentialsException ex) {
    log.error("Invalid credentials: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.AUTHENTICATION_REQUIRED))
        .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  /**
   * Handles MembershipNotFoundException - returns 404 Not Found.
   * Maneja MembershipNotFoundException - retorna 404 Not Found.
   */
  @ExceptionHandler(MembershipNotFoundException.class)
  public ResponseEntity<BaseResponse<Void>> handleMembershipNotFoundException(MembershipNotFoundException ex) {
    log.error("Membership not found: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.RESOURCE_NOT_FOUND))
        .build();

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  /**
   * Handles MembershipInactiveException - returns 403 Forbidden.
   * Maneja MembershipInactiveException - retorna 403 Forbidden.
   */
  @ExceptionHandler(MembershipInactiveException.class)
  public ResponseEntity<BaseResponse<Void>> handleMembershipInactiveException(MembershipInactiveException ex) {
    log.error("Membership inactive: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.BUSINESS_RULE_VIOLATION))
        .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  /**
   * Handles InvalidRoleAssignmentException - returns 400 Bad Request.
   * Maneja InvalidRoleAssignmentException - retorna 400 Bad Request.
   */
  @ExceptionHandler(InvalidRoleAssignmentException.class)
  public ResponseEntity<BaseResponse<Void>> handleInvalidRoleAssignmentException(InvalidRoleAssignmentException ex) {
    log.error("Invalid role assignment: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT))
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles InvalidAuthorizationCodeException - returns 400 Bad Request.
   * Maneja InvalidAuthorizationCodeException - retorna 400 Bad Request.
   */
  @ExceptionHandler(InvalidAuthorizationCodeException.class)
  public ResponseEntity<BaseResponse<Void>> handleInvalidAuthorizationCodeException(InvalidAuthorizationCodeException ex) {
    log.error("Invalid authorization code: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT))
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles AuthorizationCodeExpiredException - returns 400 Bad Request.
   * Maneja AuthorizationCodeExpiredException - retorna 400 Bad Request.
   */
  @ExceptionHandler(AuthorizationCodeExpiredException.class)
  public ResponseEntity<BaseResponse<Void>> handleAuthorizationCodeExpiredException(AuthorizationCodeExpiredException ex) {
    log.error("Authorization code expired: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT))
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles InvalidPkceVerificationException - returns 400 Bad Request.
   * Maneja InvalidPkceVerificationException - retorna 400 Bad Request.
   */
  @ExceptionHandler(InvalidPkceVerificationException.class)
  public ResponseEntity<BaseResponse<Void>> handleInvalidPkceVerificationException(InvalidPkceVerificationException ex) {
    log.error("PKCE verification failed: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT))
        .build();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  /**
   * Handles ScopeNotGrantedException - returns 403 Forbidden.
   * Maneja ScopeNotGrantedException - retorna 403 Forbidden.
   */
  @ExceptionHandler(ScopeNotGrantedException.class)
  public ResponseEntity<BaseResponse<Void>> handleScopeNotGrantedException(ScopeNotGrantedException ex) {
    log.error("Scope not granted: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.INSUFFICIENT_PERMISSIONS))
        .build();

    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
  }

  /**
   * Handles NoActiveSigningKeyException - returns 503 Service Unavailable.
   * El servidor no puede emitir tokens porque no hay clave de firma activa.
   */
  @ExceptionHandler(NoActiveSigningKeyException.class)
  public ResponseEntity<BaseResponse<Void>> handleNoActiveSigningKeyException(NoActiveSigningKeyException ex) {
    log.error("No active signing key: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.OPERATION_FAILED))
        .build();

    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
  }

  /**
   * Handles InvalidRefreshTokenException - returns 401 Unauthorized.
   */
  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<BaseResponse<Void>> handleInvalidRefreshTokenException(InvalidRefreshTokenException ex) {
    log.error("Invalid refresh token: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.AUTHENTICATION_REQUIRED))
        .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  /**
   * Handles RefreshTokenExpiredException - returns 401 Unauthorized.
   */
  @ExceptionHandler(RefreshTokenExpiredException.class)
  public ResponseEntity<BaseResponse<Void>> handleRefreshTokenExpiredException(RefreshTokenExpiredException ex) {
    log.error("Refresh token expired: {}", ex.getMessage());

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(ResponseCode.AUTHENTICATION_REQUIRED))
        .build();

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
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

