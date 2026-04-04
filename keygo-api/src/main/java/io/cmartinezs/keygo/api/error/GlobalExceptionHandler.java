package io.cmartinezs.keygo.api.error;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.auth.exception.HashingUnavailableException;
import io.cmartinezs.keygo.app.auth.exception.UnsupportedPkceMethodException;
import io.cmartinezs.keygo.app.billing.catalog.exception.DuplicatePlanCodeException;
import io.cmartinezs.keygo.app.billing.contracting.exception.ContractInvalidStateException;
import io.cmartinezs.keygo.app.billing.contracting.exception.ContractNotFoundException;
import io.cmartinezs.keygo.app.billing.contracting.exception.PlanVersionNotFoundException;
import io.cmartinezs.keygo.app.billing.contracting.exception.ProviderAppNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.exception.SubscriptionInvalidStateException;
import io.cmartinezs.keygo.app.billing.subscription.exception.SubscriptionNotFoundException;
import io.cmartinezs.keygo.app.clientapp.exception.ClientAppInactiveException;
import io.cmartinezs.keygo.app.membership.exception.DuplicateAppRoleException;
import io.cmartinezs.keygo.app.membership.exception.DuplicateMembershipException;
import io.cmartinezs.keygo.app.membership.exception.InvalidCommandFieldException;
import io.cmartinezs.keygo.app.shared.exception.PortException;
import io.cmartinezs.keygo.app.shared.exception.UseCaseException;
import io.cmartinezs.keygo.app.tenant.exception.DuplicateTenantException;
import io.cmartinezs.keygo.app.tenant.exception.InvalidPaginationParamException;
import io.cmartinezs.keygo.app.user.exception.IncorrectCurrentPasswordException;
import io.cmartinezs.keygo.app.user.exception.UserNotInResetPasswordStatusException;
import io.cmartinezs.keygo.domain.auth.exception.AuthorizationCodeExpiredException;
import io.cmartinezs.keygo.domain.auth.exception.InvalidAuthorizationCodeException;
import io.cmartinezs.keygo.domain.auth.exception.InvalidPkceVerificationException;
import io.cmartinezs.keygo.domain.auth.exception.InvalidRefreshTokenException;
import io.cmartinezs.keygo.domain.auth.exception.NoActiveSigningKeyException;
import io.cmartinezs.keygo.domain.auth.exception.RefreshTokenExpiredException;
import io.cmartinezs.keygo.domain.auth.exception.ScopeNotGrantedException;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAuthenticationException;
import io.cmartinezs.keygo.domain.clientapp.exception.InvalidRedirectUriException;
import io.cmartinezs.keygo.domain.clientapp.exception.UnsupportedGrantTypeException;
import io.cmartinezs.keygo.domain.membership.exception.InvalidRoleAssignmentException;
import io.cmartinezs.keygo.domain.membership.exception.MembershipInactiveException;
import io.cmartinezs.keygo.domain.membership.exception.MembershipNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.user.exception.DuplicateUserException;
import io.cmartinezs.keygo.domain.user.exception.EmailVerificationExpiredException;
import io.cmartinezs.keygo.domain.user.exception.EmailVerificationInvalidException;
import io.cmartinezs.keygo.domain.user.exception.EmailVerificationStillActiveException;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import io.cmartinezs.keygo.domain.user.exception.InvalidPasswordResetCodeException;
import io.cmartinezs.keygo.domain.user.exception.PasswordRecoveryTokenAlreadyUsedException;
import io.cmartinezs.keygo.domain.user.exception.PasswordRecoveryTokenExpiredException;
import io.cmartinezs.keygo.domain.user.exception.PasswordResetCodeExpiredException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserPasswordResetRequiredException;
import io.cmartinezs.keygo.domain.user.exception.UserPendingVerificationException;
import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final Environment environment;
  private final ApiErrorDataFactory apiErrorDataFactory;
  /**
   * Handles UnauthorizedException - returns 401 Unauthorized.
   * Maneja UnauthorizedException - retorna 401 Unauthorized.
   *
   * @param ex the exception / la excepción
   * @return ResponseEntity with error details / ResponseEntity con detalles del error
   */
  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleUnauthorizedException(UnauthorizedException ex) {
    log.error("Unauthorized access attempt: {}", ex.getMessage());
    return error(HttpStatus.UNAUTHORIZED, ResponseCode.AUTHENTICATION_REQUIRED, ex);
  }

  /**
   * Handles NoResourceFoundException - returns 404 Not Found.
   * Maneja NoResourceFoundException - retorna 404 Not Found.
   *
   * @param ex the exception / la excepción
   * @return ResponseEntity with error details / ResponseEntity con detalles del error
   */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleNoResourceFoundException(NoResourceFoundException ex) {
    log.error("Resource not found: {}", ex.getMessage());
    return error(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_FOUND, ex);
  }

  /**
   * Handles IllegalArgumentException - returns 400 Bad Request.
   * Maneja IllegalArgumentException - retorna 400 Bad Request.
   *
   * @param ex the exception / la excepción
   * @return ResponseEntity with error details / ResponseEntity con detalles del error
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.error("Invalid argument: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Handles @Valid validation errors on request bodies - returns 400 Bad Request.
   * Extracts per-field validation messages from BindingResult.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleValidationException(MethodArgumentNotValidException ex) {
    log.error("Validation failed: {}", ex.getMessage());
    boolean techDetails = includeTechnicalDetails();
    List<FieldValidationError> fieldErrors = new ArrayList<>();

    ex.getBindingResult().getFieldErrors().forEach(fe ->
        fieldErrors.add(FieldValidationError.builder()
            .field(fe.getField())
            .message(fe.getDefaultMessage())
            .rejectedValue(techDetails ? fe.getRejectedValue() : null)
            .build())
    );

    ex.getBindingResult().getGlobalErrors().forEach(ge ->
        fieldErrors.add(FieldValidationError.builder()
            .field(ge.getObjectName())
            .message(ge.getDefaultMessage())
            .build())
    );

    ErrorData errorData = apiErrorDataFactory.fromValidationErrors(
        ResponseCode.INVALID_INPUT, fieldErrors, techDetails);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(BaseResponse.<ErrorData>builder()
            .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT))
            .data(errorData)
            .build());
  }

  /**
   * Handles @Validated constraint violations on method parameters (query params, path vars, headers).
   * Returns 400 Bad Request with per-parameter violation details.
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleConstraintViolationException(ConstraintViolationException ex) {
    log.error("Constraint violation: {}", ex.getMessage());
    boolean techDetails = includeTechnicalDetails();
    List<FieldValidationError> fieldErrors = new ArrayList<>();

    ex.getConstraintViolations().forEach(cv -> {
      String path = cv.getPropertyPath().toString();
      // Strip method name prefix: "methodName.paramName" → "paramName"
      String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
      fieldErrors.add(FieldValidationError.builder()
          .field(field)
          .message(cv.getMessage())
          .rejectedValue(techDetails ? cv.getInvalidValue() : null)
          .build());
    });

    ErrorData errorData = apiErrorDataFactory.fromValidationErrors(
        ResponseCode.INVALID_INPUT, fieldErrors, techDetails);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(BaseResponse.<ErrorData>builder()
            .failure(ResponseHelper.message(ResponseCode.INVALID_INPUT))
            .data(errorData)
            .build());
  }

  /**
   * Handles missing request parameters - returns 400 Bad Request.
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException ex) {
    log.error("Missing request parameter: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Handles malformed JSON payloads - returns 400 Bad Request.
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex) {
    log.error("Malformed request payload: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Handles TenantNotFoundException - returns 404 Not Found.
   * Maneja TenantNotFoundException - retorna 404 Not Found.
   */
  @ExceptionHandler(TenantNotFoundException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleTenantNotFoundException(TenantNotFoundException ex) {
    log.error("Tenant not found: {}", ex.getMessage());
    return error(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_FOUND, ex);
  }

  /**
   * Handles TenantSuspendedException - returns 403 Forbidden.
   * Maneja TenantSuspendedException - retorna 403 Forbidden.
   */
  @ExceptionHandler(TenantSuspendedException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleTenantSuspendedException(TenantSuspendedException ex) {
    log.error("Tenant suspended: {}", ex.getMessage());
    return error(HttpStatus.FORBIDDEN, ResponseCode.BUSINESS_RULE_VIOLATION, ex);
  }

  /**
   * Handles ClientAuthenticationException - returns 401 Unauthorized.
   * Lanzada cuando el client_secret es incorrecto o el cliente es PUBLIC en un grant M2M.
   */
  @ExceptionHandler(ClientAuthenticationException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleClientAuthenticationException(ClientAuthenticationException ex) {
    log.error("Client authentication failed: {}", ex.getMessage());
    return error(HttpStatus.UNAUTHORIZED, ResponseCode.AUTHENTICATION_REQUIRED, ex);
  }

  /**
   * Handles ClientAppNotFoundException - returns 404 Not Found.
   * Maneja ClientAppNotFoundException - retorna 404 Not Found.
   */
  @ExceptionHandler(ClientAppNotFoundException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleClientAppNotFoundException(ClientAppNotFoundException ex) {
    log.error("Client app not found: {}", ex.getMessage());
    return error(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_FOUND, ex);
  }

  /**
   * Handles InvalidRedirectUriException - returns 400 Bad Request.
   * Maneja InvalidRedirectUriException - retorna 400 Bad Request.
   */
  @ExceptionHandler(InvalidRedirectUriException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleInvalidRedirectUriException(InvalidRedirectUriException ex) {
    log.error("Invalid redirect URI: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Handles UnsupportedGrantTypeException - returns 400 Bad Request.
   * Maneja UnsupportedGrantTypeException - retorna 400 Bad Request.
   */
  @ExceptionHandler(UnsupportedGrantTypeException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleUnsupportedGrantTypeException(UnsupportedGrantTypeException ex) {
    log.error("Unsupported grant type: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Handles UserNotFoundException - returns 404 Not Found.
   * Maneja UserNotFoundException - retorna 404 Not Found.
   */
  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleUserNotFoundException(UserNotFoundException ex) {
    log.error("User not found: {}", ex.getMessage());
    return error(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_FOUND, ex);
  }

  /**
   * Handles UserSuspendedException - returns 403 Forbidden.
   * Maneja UserSuspendedException - retorna 403 Forbidden.
   */
  @ExceptionHandler(UserSuspendedException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleUserSuspendedException(UserSuspendedException ex) {
    log.error("User suspended: {}", ex.getMessage());
    return error(HttpStatus.FORBIDDEN, ResponseCode.BUSINESS_RULE_VIOLATION, ex);
  }

  /**
   * Handles DuplicateUserException - returns 409 Conflict.
   * Maneja DuplicateUserException - retorna 409 Conflict.
   */
  @ExceptionHandler(DuplicateUserException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleDuplicateUserException(DuplicateUserException ex) {
    log.error("Duplicate user: {}", ex.getMessage());
    return error(HttpStatus.CONFLICT, ResponseCode.DUPLICATE_RESOURCE, ex);
  }

  /**
   * Handles InvalidCredentialsException - returns 401 Unauthorized.
   * Maneja InvalidCredentialsException - retorna 401 Unauthorized.
   */
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleInvalidCredentialsException(InvalidCredentialsException ex) {
    log.error("Invalid credentials: {}", ex.getMessage());
    return error(HttpStatus.UNAUTHORIZED, ResponseCode.AUTHENTICATION_REQUIRED, ex);
  }

  /**
   * Handles MembershipNotFoundException - returns 404 Not Found.
   * Maneja MembershipNotFoundException - retorna 404 Not Found.
   */
  @ExceptionHandler(MembershipNotFoundException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleMembershipNotFoundException(MembershipNotFoundException ex) {
    log.error("Membership not found: {}", ex.getMessage());
    return error(HttpStatus.NOT_FOUND, ResponseCode.RESOURCE_NOT_FOUND, ex);
  }

  /**
   * Handles MembershipInactiveException - returns 403 Forbidden.
   * Maneja MembershipInactiveException - retorna 403 Forbidden.
   */
  @ExceptionHandler(MembershipInactiveException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleMembershipInactiveException(MembershipInactiveException ex) {
    log.error("Membership inactive: {}", ex.getMessage());
    return error(HttpStatus.FORBIDDEN, ResponseCode.BUSINESS_RULE_VIOLATION, ex);
  }

  /**
   * Handles InvalidRoleAssignmentException - returns 400 Bad Request.
   * Maneja InvalidRoleAssignmentException - retorna 400 Bad Request.
   */
  @ExceptionHandler(InvalidRoleAssignmentException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleInvalidRoleAssignmentException(InvalidRoleAssignmentException ex) {
    log.error("Invalid role assignment: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Handles InvalidAuthorizationCodeException - returns 400 Bad Request.
   * Maneja InvalidAuthorizationCodeException - retorna 400 Bad Request.
   */
  @ExceptionHandler(InvalidAuthorizationCodeException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleInvalidAuthorizationCodeException(InvalidAuthorizationCodeException ex) {
    log.error("Invalid authorization code: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Handles AuthorizationCodeExpiredException - returns 400 Bad Request.
   * Maneja AuthorizationCodeExpiredException - retorna 400 Bad Request.
   */
  @ExceptionHandler(AuthorizationCodeExpiredException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleAuthorizationCodeExpiredException(AuthorizationCodeExpiredException ex) {
    log.error("Authorization code expired: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Handles InvalidPkceVerificationException - returns 400 Bad Request.
   * Maneja InvalidPkceVerificationException - retorna 400 Bad Request.
   */
  @ExceptionHandler(InvalidPkceVerificationException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleInvalidPkceVerificationException(InvalidPkceVerificationException ex) {
    log.error("PKCE verification failed: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Handles ScopeNotGrantedException - returns 403 Forbidden.
   * Maneja ScopeNotGrantedException - retorna 403 Forbidden.
   */
  @ExceptionHandler(ScopeNotGrantedException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleScopeNotGrantedException(ScopeNotGrantedException ex) {
    log.error("Scope not granted: {}", ex.getMessage());
    return error(HttpStatus.FORBIDDEN, ResponseCode.INSUFFICIENT_PERMISSIONS, ex);
  }

  /**
   * Handles NoActiveSigningKeyException - returns 503 Service Unavailable.
   * El servidor no puede emitir tokens porque no hay clave de firma activa.
   */
  @ExceptionHandler(NoActiveSigningKeyException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleNoActiveSigningKeyException(NoActiveSigningKeyException ex) {
    log.error("No active signing key: {}", ex.getMessage());
    return error(HttpStatus.SERVICE_UNAVAILABLE, ResponseCode.OPERATION_FAILED, ex);
  }

  /**
   * Handles InvalidRefreshTokenException - returns 401 Unauthorized.
   */
  @ExceptionHandler(InvalidRefreshTokenException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleInvalidRefreshTokenException(InvalidRefreshTokenException ex) {
    log.error("Invalid refresh token: {}", ex.getMessage());
    return error(HttpStatus.UNAUTHORIZED, ResponseCode.AUTHENTICATION_REQUIRED, ex);
  }

  /**
   * Handles RefreshTokenExpiredException - returns 401 Unauthorized.
   */
  @ExceptionHandler(RefreshTokenExpiredException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleRefreshTokenExpiredException(RefreshTokenExpiredException ex) {
    log.error("Refresh token expired: {}", ex.getMessage());
    return error(HttpStatus.UNAUTHORIZED, ResponseCode.AUTHENTICATION_REQUIRED, ex);
  }

  /**
   * Handles UserPendingVerificationException - returns 403 Forbidden.
   * The user exists but has not verified their email yet.
   * Maneja UserPendingVerificationException - retorna 403 Forbidden.
   */
  @ExceptionHandler(UserPendingVerificationException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleUserPendingVerificationException(
      UserPendingVerificationException ex) {
    log.warn("Login attempt by unverified user: {}", ex.getMessage());
    return error(HttpStatus.FORBIDDEN, ResponseCode.EMAIL_NOT_VERIFIED, ex);
  }

  /**
   * Handles EmailVerificationExpiredException - returns 422 Unprocessable Entity.
   * The verification code has expired; user must request a new one.
   * Maneja EmailVerificationExpiredException - retorna 422.
   */
  @ExceptionHandler(EmailVerificationExpiredException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleEmailVerificationExpiredException(
      EmailVerificationExpiredException ex) {
    log.warn("Expired verification code used: {}", ex.getMessage());
    return error(HttpStatus.UNPROCESSABLE_CONTENT, ResponseCode.EMAIL_VERIFICATION_EXPIRED, ex);
  }

  /**
   * Handles EmailVerificationInvalidException - returns 400 Bad Request.
   * The code is wrong or already used.
   * Maneja EmailVerificationInvalidException - retorna 400.
   */
  @ExceptionHandler(EmailVerificationInvalidException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleEmailVerificationInvalidException(
      EmailVerificationInvalidException ex) {
    log.warn("Invalid verification code: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Handles EmailVerificationStillActiveException - returns 409 Conflict.
   * The current verification code is still active; resend is not allowed yet.
   * Maneja EmailVerificationStillActiveException - retorna 409.
   */
  @ExceptionHandler(EmailVerificationStillActiveException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleEmailVerificationStillActiveException(
      EmailVerificationStillActiveException ex) {
    log.warn("Resend blocked — code still active: {}", ex.getMessage());
    return error(HttpStatus.CONFLICT, ResponseCode.EMAIL_VERIFICATION_STILL_ACTIVE, ex);
  }

  /**
   * Handles AccessDeniedException (including AuthorizationDeniedException from @PreAuthorize).
   * Returns 403 Forbidden with a structured BaseResponse.
   * <p>Maneja AccessDeniedException (incluye AuthorizationDeniedException de @PreAuthorize).
   * Retorna 403 Forbidden con un BaseResponse estructurado.
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleAccessDeniedException(AccessDeniedException ex) {
    log.warn("Access denied: {}", ex.getMessage());
    return error(HttpStatus.FORBIDDEN, ResponseCode.INSUFFICIENT_PERMISSIONS, ex);
  }

  /**
   * Handles ContractNotFoundException - returns 404 Not Found.
   */
  @ExceptionHandler(ContractNotFoundException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleContractNotFoundException(ContractNotFoundException ex) {
    log.error("Contract not found: {}", ex.getMessage());
    return error(HttpStatus.NOT_FOUND, ResponseCode.CONTRACT_NOT_FOUND, ex);
  }

  /**
   * Handles ProviderAppNotFoundException - returns 404 Not Found.
   */
  @ExceptionHandler(ProviderAppNotFoundException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleProviderAppNotFoundException(ProviderAppNotFoundException ex) {
    log.error("Provider app not found: {}", ex.getMessage());
    return error(HttpStatus.NOT_FOUND, ResponseCode.PROVIDER_APP_NOT_FOUND, ex);
  }

  /**
   * Handles ContractInvalidStateException - returns 422 Unprocessable Entity.
   */
  @ExceptionHandler(ContractInvalidStateException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleContractInvalidStateException(ContractInvalidStateException ex) {
    log.error("Contract invalid state: {}", ex.getMessage());
    return error(HttpStatus.UNPROCESSABLE_CONTENT, ResponseCode.CONTRACT_INVALID_STATE, ex);
  }

  /**
   * Handles PlanVersionNotFoundException - returns 404 Not Found.
   */
  @ExceptionHandler(PlanVersionNotFoundException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handlePlanVersionNotFoundException(PlanVersionNotFoundException ex) {
    log.error("Plan version not found: {}", ex.getMessage());
    return error(HttpStatus.NOT_FOUND, ResponseCode.PLAN_VERSION_NOT_FOUND, ex);
  }

  /**
   * Handles DuplicatePlanCodeException - returns 409 Conflict.
   */
  @ExceptionHandler(DuplicatePlanCodeException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleDuplicatePlanCodeException(DuplicatePlanCodeException ex) {
    log.error("Duplicate plan code: {}", ex.getMessage());
    return error(HttpStatus.CONFLICT, ResponseCode.DUPLICATE_RESOURCE, ex);
  }

  /**
   * Handles SubscriptionNotFoundException - returns 404 Not Found.
   */
  @ExceptionHandler(SubscriptionNotFoundException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleSubscriptionNotFoundException(SubscriptionNotFoundException ex) {
    log.error("Subscription not found: {}", ex.getMessage());
    return error(HttpStatus.NOT_FOUND, ResponseCode.SUBSCRIPTION_NOT_FOUND, ex);
  }

  /**
   * Handles SubscriptionInvalidStateException - returns 422 Unprocessable Entity.
   */
  @ExceptionHandler(SubscriptionInvalidStateException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleSubscriptionInvalidStateException(SubscriptionInvalidStateException ex) {
    log.error("Subscription invalid state: {}", ex.getMessage());
    return error(HttpStatus.UNPROCESSABLE_CONTENT, ResponseCode.SUBSCRIPTION_INVALID_STATE, ex);
  }

  /**
   * Handles UnsupportedPkceMethodException - returns 400 Bad Request.
   */
  @ExceptionHandler(UnsupportedPkceMethodException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleUnsupportedPkceMethodException(UnsupportedPkceMethodException ex) {
    log.error("Unsupported PKCE method: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.UNSUPPORTED_PKCE_METHOD, ex);
  }

  /**
   * Handles HashingUnavailableException - returns 503 Service Unavailable.
   */
  @ExceptionHandler(HashingUnavailableException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleHashingUnavailableException(HashingUnavailableException ex) {
    log.error("Hashing unavailable: {}", ex.getMessage(), ex);
    return error(HttpStatus.SERVICE_UNAVAILABLE, ResponseCode.EXTERNAL_SERVICE_ERROR, ex);
  }

  /**
   * Handles DuplicateAppRoleException - returns 409 Conflict.
   */
  @ExceptionHandler(DuplicateAppRoleException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleDuplicateAppRoleException(DuplicateAppRoleException ex) {
    log.error("Duplicate app role: {}", ex.getMessage());
    return error(HttpStatus.CONFLICT, ResponseCode.DUPLICATE_RESOURCE, ex);
  }

  /**
   * Handles DuplicateMembershipException - returns 409 Conflict.
   */
  @ExceptionHandler(DuplicateMembershipException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleDuplicateMembershipException(DuplicateMembershipException ex) {
    log.error("Duplicate membership: {}", ex.getMessage());
    return error(HttpStatus.CONFLICT, ResponseCode.DUPLICATE_RESOURCE, ex);
  }

  /**
   * Handles IncorrectCurrentPasswordException - returns 403 Forbidden.
   * Lanzada cuando el usuario proporciona una contraseña actual incorrecta al hacer self-service
   * de cambio de contraseña.
   */
  @ExceptionHandler(IncorrectCurrentPasswordException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleIncorrectCurrentPasswordException(
      IncorrectCurrentPasswordException ex) {
    log.warn("Incorrect current password attempt: {}", ex.getMessage());
    return error(HttpStatus.FORBIDDEN, ResponseCode.BUSINESS_RULE_VIOLATION, ex);
  }

  /**
   * Handles UserNotInResetPasswordStatusException - returns 403 Forbidden.
   * Lanzada cuando se intenta restablecer la contraseña de un usuario que no está en
   * estado RESET_PASSWORD.
   */
  @ExceptionHandler(UserNotInResetPasswordStatusException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleUserNotInResetPasswordStatusException(
      UserNotInResetPasswordStatusException ex) {
    log.warn("Reset password rejected — user not in RESET_PASSWORD status: {}", ex.getMessage());
    return error(HttpStatus.FORBIDDEN, ResponseCode.BUSINESS_RULE_VIOLATION, ex);
  }

  /**
   * Handles PasswordRecoveryTokenExpiredException - returns 422 Unprocessable Entity.
   * Lanzada cuando el token de recuperación de contraseña ha expirado.
   */
  @ExceptionHandler(PasswordRecoveryTokenExpiredException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handlePasswordRecoveryTokenExpiredException(
      PasswordRecoveryTokenExpiredException ex) {
    log.warn("Recovery token expired: {}", ex.getMessage());
    return error(HttpStatus.UNPROCESSABLE_CONTENT, ResponseCode.BUSINESS_RULE_VIOLATION, ex);
  }

  /**
   * Handles PasswordRecoveryTokenAlreadyUsedException - returns 422 Unprocessable Entity.
   * Lanzada cuando el token de recuperación de contraseña ya fue utilizado.
   */
  @ExceptionHandler(PasswordRecoveryTokenAlreadyUsedException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handlePasswordRecoveryTokenAlreadyUsedException(
      PasswordRecoveryTokenAlreadyUsedException ex) {
    log.warn("Recovery token already used: {}", ex.getMessage());
    return error(HttpStatus.UNPROCESSABLE_CONTENT, ResponseCode.BUSINESS_RULE_VIOLATION, ex);
  }

  /**
   * Handles UserPasswordResetRequiredException - returns 401 Unauthorized.
   * Lanzada cuando un usuario con status=RESET_PASSWORD intenta autenticarse.
   * El cliente debe redirigir al flujo de reset de contraseña.
   * NOTA: el controlador de login la captura antes y envía el email, pero se mantiene
   * este handler como fallback para otros contextos donde pueda propagarse.
   */
  @ExceptionHandler(UserPasswordResetRequiredException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleUserPasswordResetRequiredException(
      UserPasswordResetRequiredException ex) {
    log.warn("Login blocked — password reset required: {}", ex.getMessage());
    return error(HttpStatus.UNAUTHORIZED, ResponseCode.RESET_PASSWORD_REQUIRED, ex);
  }

  /**
   * Handles InvalidPasswordResetCodeException - returns 400 Bad Request.
   * Lanzada cuando el código de verificación de reset de contraseña es inválido o no existe.
   */
  @ExceptionHandler(InvalidPasswordResetCodeException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleInvalidPasswordResetCodeException(
      InvalidPasswordResetCodeException ex) {
    log.warn("Invalid password reset code: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Handles PasswordResetCodeExpiredException - returns 422 Unprocessable Entity.
   * Lanzada cuando el código de verificación de reset de contraseña ha expirado.
   */
  @ExceptionHandler(PasswordResetCodeExpiredException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handlePasswordResetCodeExpiredException(
      PasswordResetCodeExpiredException ex) {
    log.warn("Password reset code expired: {}", ex.getMessage());
    return error(HttpStatus.UNPROCESSABLE_CONTENT, ResponseCode.BUSINESS_RULE_VIOLATION, ex);
  }

  /**
   * Handles SecurityException - returns 403 Forbidden.
   * Lanzada cuando un usuario intenta operar sobre un recurso que no le pertenece
   * (p.ej. revocar una sesión de otro usuario).
   */
  @ExceptionHandler(SecurityException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleSecurityException(SecurityException ex) {
    log.warn("Security violation: {}", ex.getMessage());
    return error(HttpStatus.FORBIDDEN, ResponseCode.BUSINESS_RULE_VIOLATION, ex);
  }

  /**
   * Handles InvalidCommandFieldException - returns 400 Bad Request.
   */
  @ExceptionHandler(InvalidCommandFieldException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleInvalidCommandFieldException(InvalidCommandFieldException ex) {
    log.error("Invalid command field: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Handles ClientAppInactiveException - returns 422 Unprocessable Entity.
   */
  @ExceptionHandler(ClientAppInactiveException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleClientAppInactiveException(ClientAppInactiveException ex) {
    log.error("Client app inactive: {}", ex.getMessage());
    return error(HttpStatus.UNPROCESSABLE_CONTENT, ResponseCode.CLIENT_APP_INACTIVE, ex);
  }

  /**
   * Handles DuplicateTenantException - returns 409 Conflict.
   */
  @ExceptionHandler(DuplicateTenantException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleDuplicateTenantException(DuplicateTenantException ex) {
    log.error("Duplicate tenant: {}", ex.getMessage());
    return error(HttpStatus.CONFLICT, ResponseCode.DUPLICATE_TENANT, ex);
  }

  /**
   * Handles InvalidPaginationParamException - returns 400 Bad Request.
   */
  @ExceptionHandler(InvalidPaginationParamException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleInvalidPaginationParamException(InvalidPaginationParamException ex) {
    log.error("Invalid pagination param: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, ResponseCode.INVALID_INPUT, ex);
  }

  /**
   * Catch-all for UseCaseException not handled by a specific handler - returns 500.
   */
  @ExceptionHandler(UseCaseException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleUseCaseException(UseCaseException ex) {
    log.error("Use case error: {}", ex.getMessage(), ex);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.OPERATION_FAILED, ex);
  }

  /**
   * Handles PortException (outbound port failure) - returns 503 Service Unavailable.
   */
  @ExceptionHandler(PortException.class)
  public ResponseEntity<BaseResponse<ErrorData>> handlePortException(PortException ex) {
    log.error("Port error: {}", ex.getMessage(), ex);
    return error(HttpStatus.SERVICE_UNAVAILABLE, ResponseCode.EXTERNAL_SERVICE_ERROR, ex);
  }

  /**
   * Handles generic exceptions - returns 500 Internal Server Error.
   * Maneja excepciones genéricas - retorna 500 Internal Server Error.
   *
   * @param ex the exception / la excepción
   * @return ResponseEntity with error details / ResponseEntity con detalles del error
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BaseResponse<ErrorData>> handleGenericException(Exception ex) {
    log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, ResponseCode.OPERATION_FAILED, ex);
  }

  private ResponseEntity<BaseResponse<ErrorData>> error(
      HttpStatus status,
      ResponseCode responseCode,
      Throwable throwable) {
    BaseResponse<ErrorData> response = BaseResponse.<ErrorData>builder()
        .failure(ResponseHelper.message(responseCode))
        .data(apiErrorDataFactory.fromException(responseCode, throwable, includeTechnicalDetails()))
        .build();

    return ResponseEntity.status(status).body(response);
  }

  private boolean includeTechnicalDetails() {
    return environment.acceptsProfiles(Profiles.of("local", "dev"));
  }
}

