package io.cmartinezs.keygo.api.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.api.shared.MessageTranslator;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.user.exception.UserNotInResetPasswordStatusException;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import io.cmartinezs.keygo.domain.user.exception.PasswordRecoveryTokenAlreadyUsedException;
import io.cmartinezs.keygo.domain.user.exception.PasswordRecoveryTokenExpiredException;
import io.cmartinezs.keygo.domain.user.exception.UserPasswordResetRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Unit tests for GlobalExceptionHandler Pruebas unitarias para GlobalExceptionHandler
 *
 * @author cmartinezs
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @Mock private Environment environment;

  private ApiErrorDataFactory factory;
  private GlobalExceptionHandler handler;

  @BeforeEach
  void setUp() {
    factory = new ApiErrorDataFactory(new MessageTranslator(new StaticMessageSource()));
    handler = new GlobalExceptionHandler(environment, factory);
    lenient().when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);
  }

  @Test
  void handleUnauthorizedException_shouldReturnUnauthorized() {
    // Given
    UnauthorizedException exception = new UnauthorizedException("Unauthorized access");

    // When
    ResponseEntity<BaseResponse<ErrorData>> response =
        handler.handleUnauthorizedException(exception);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().getOrigin()).isEqualTo(ApiErrorOrigin.CLIENT_REQUEST);
    assertThat(response.getBody().getData().getClientRequestCause())
        .isEqualTo(ApiClientRequestCause.CLIENT_TECHNICAL);
    assertThat(response.getBody().getData().getClientMessage()).isNotBlank();
    assertThat(response.getBody().getData().getDetail()).isNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.AUTHENTICATION_REQUIRED.getCode());
  }

  @Test
  void handleNoResourceFoundException_shouldReturnNotFound() {
    // Given
    NoResourceFoundException exception =
        new NoResourceFoundException(HttpMethod.GET, "/test", "test");

    // When
    ResponseEntity<BaseResponse<ErrorData>> response =
        handler.handleNoResourceFoundException(exception);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().getOrigin()).isEqualTo(ApiErrorOrigin.CLIENT_REQUEST);
    assertThat(response.getBody().getData().getClientRequestCause())
        .isEqualTo(ApiClientRequestCause.CLIENT_TECHNICAL);
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.RESOURCE_NOT_FOUND.getCode());
  }

  @Test
  void handleIllegalArgumentException_shouldReturnBadRequest() {
    // Given
    IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

    // When
    ResponseEntity<BaseResponse<ErrorData>> response =
        handler.handleIllegalArgumentException(exception);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().getOrigin()).isEqualTo(ApiErrorOrigin.CLIENT_REQUEST);
    assertThat(response.getBody().getData().getClientRequestCause())
        .isEqualTo(ApiClientRequestCause.USER_INPUT);
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.INVALID_INPUT.getCode());
  }

  @Test
  void handleGenericException_shouldReturnInternalServerError() {
    // Given
    Exception exception = new Exception("Generic error");

    // When
    ResponseEntity<BaseResponse<ErrorData>> response = handler.handleGenericException(exception);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().getOrigin())
        .isEqualTo(ApiErrorOrigin.SERVER_PROCESSING);
    assertThat(response.getBody().getData().getClientRequestCause()).isNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.OPERATION_FAILED.getCode());
  }

  @Test
  void handleInvalidCredentialsException_shouldClassifyAsUserInput() {
    // Given
    InvalidCredentialsException exception = new InvalidCredentialsException();

    // When
    ResponseEntity<BaseResponse<ErrorData>> response =
        handler.handleInvalidCredentialsException(exception);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().getOrigin()).isEqualTo(ApiErrorOrigin.CLIENT_REQUEST);
    assertThat(response.getBody().getData().getClientRequestCause())
        .isEqualTo(ApiClientRequestCause.USER_INPUT);
  }

  @Test
  void handleMissingServletRequestParameterException_shouldClassifyAsClientTechnical() {
    // Given
    MissingServletRequestParameterException exception =
        new MissingServletRequestParameterException("response_type", "String");

    // When
    ResponseEntity<BaseResponse<ErrorData>> response =
        handler.handleMissingServletRequestParameterException(exception);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().getOrigin()).isEqualTo(ApiErrorOrigin.CLIENT_REQUEST);
    assertThat(response.getBody().getData().getClientRequestCause())
        .isEqualTo(ApiClientRequestCause.CLIENT_TECHNICAL);
  }

  @Test
  void handleHttpMessageNotReadableException_shouldClassifyAsClientTechnical() {
    // Given
    HttpMessageNotReadableException exception =
        new HttpMessageNotReadableException(
            "Malformed JSON", new MockHttpInputMessage(new byte[0]));

    // When
    ResponseEntity<BaseResponse<ErrorData>> response =
        handler.handleHttpMessageNotReadableException(exception);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().getOrigin()).isEqualTo(ApiErrorOrigin.CLIENT_REQUEST);
    assertThat(response.getBody().getData().getClientRequestCause())
        .isEqualTo(ApiClientRequestCause.CLIENT_TECHNICAL);
  }

  @Test
  void handleGenericException_inLocalProfile_shouldIncludeTechnicalDetail() {
    // Given
    when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);
    Exception exception = new Exception("db timeout");

    // When
    ResponseEntity<BaseResponse<ErrorData>> response = handler.handleGenericException(exception);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().getDetail()).isEqualTo("db timeout");
    assertThat(response.getBody().getData().getException()).isEqualTo("Exception");
  }

  @Test
  void allHandlers_shouldReturnNonNullResponse() {
    // Given
    UnauthorizedException unauth = new UnauthorizedException("test");
    NoResourceFoundException notFound =
        new NoResourceFoundException(HttpMethod.GET, "/test", "test");
    IllegalArgumentException illegalArg = new IllegalArgumentException("test");
    Exception generic = new Exception("test");

    // When / Then
    assertThat(handler.handleUnauthorizedException(unauth).getBody()).isNotNull();
    assertThat(handler.handleNoResourceFoundException(notFound).getBody()).isNotNull();
    assertThat(handler.handleIllegalArgumentException(illegalArg).getBody()).isNotNull();
    assertThat(handler.handleGenericException(generic).getBody()).isNotNull();
  }

  @Test
  void allHandlers_shouldHaveFailureMessage() {
    // Given
    UnauthorizedException unauth = new UnauthorizedException("test");
    NoResourceFoundException notFound =
        new NoResourceFoundException(HttpMethod.GET, "/test", "test");
    IllegalArgumentException illegalArg = new IllegalArgumentException("test");
    Exception generic = new Exception("test");

    // When / Then
    assertThat(handler.handleUnauthorizedException(unauth).getBody().getFailure()).isNotNull();
    assertThat(handler.handleNoResourceFoundException(notFound).getBody().getFailure()).isNotNull();
    assertThat(handler.handleIllegalArgumentException(illegalArg).getBody().getFailure())
        .isNotNull();
    assertThat(handler.handleGenericException(generic).getBody().getFailure()).isNotNull();
  }

  // ─── Password flow handlers ────────────────────────────────────────────────

  @Test
  void handleUserPasswordResetRequiredException_returns403() {
    var ex = new UserPasswordResetRequiredException("johndoe");
    var response = handler.handleUserPasswordResetRequiredException(ex);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.RESET_PASSWORD_REQUIRED.getCode());
  }

  @Test
  void handleUserNotInResetPasswordStatusException_returns403() {
    var ex = new UserNotInResetPasswordStatusException("john@acme.com");
    var response = handler.handleUserNotInResetPasswordStatusException(ex);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.BUSINESS_RULE_VIOLATION.getCode());
  }

  @Test
  void handlePasswordRecoveryTokenExpiredException_returns422() {
    var ex = new PasswordRecoveryTokenExpiredException();
    var response = handler.handlePasswordRecoveryTokenExpiredException(ex);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.BUSINESS_RULE_VIOLATION.getCode());
  }

  @Test
  void handlePasswordRecoveryTokenAlreadyUsedException_returns422() {
    var ex = new PasswordRecoveryTokenAlreadyUsedException();
    var response = handler.handlePasswordRecoveryTokenAlreadyUsedException(ex);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.BUSINESS_RULE_VIOLATION.getCode());
  }
}
