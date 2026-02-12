package io.cmartinezs.keygo.api.exception;

import io.cmartinezs.keygo.api.constant.ResponseCode;
import io.cmartinezs.keygo.api.dto.reponse.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GlobalExceptionHandler
 * Pruebas unitarias para GlobalExceptionHandler
 *
 * @author cmartinezs
 * @version 1.0
 */
class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
  }

  @Test
  void handleUnauthorizedException_shouldReturnUnauthorized() {
    // Given
    UnauthorizedException exception = new UnauthorizedException("Unauthorized access");

    // When
    ResponseEntity<BaseResponse<Void>> response = handler.handleUnauthorizedException(exception);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.AUTHENTICATION_REQUIRED.getCode());
  }

  @Test
  void handleNoResourceFoundException_shouldReturnNotFound() {
    // Given
    NoResourceFoundException exception = new NoResourceFoundException(HttpMethod.GET, "/test", "test");

    // When
    ResponseEntity<BaseResponse<Void>> response = handler.handleNoResourceFoundException(exception);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.RESOURCE_NOT_FOUND.getCode());
  }

  @Test
  void handleIllegalArgumentException_shouldReturnBadRequest() {
    // Given
    IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

    // When
    ResponseEntity<BaseResponse<Void>> response = handler.handleIllegalArgumentException(exception);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.INVALID_INPUT.getCode());
  }

  @Test
  void handleGenericException_shouldReturnInternalServerError() {
    // Given
    Exception exception = new Exception("Generic error");

    // When
    ResponseEntity<BaseResponse<Void>> response = handler.handleGenericException(exception);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.OPERATION_FAILED.getCode());
  }

  @Test
  void allHandlers_shouldReturnNonNullResponse() {
    // Given
    UnauthorizedException unauth = new UnauthorizedException("test");
    NoResourceFoundException notFound = new NoResourceFoundException(HttpMethod.GET, "/test", "test");
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
    NoResourceFoundException notFound = new NoResourceFoundException(HttpMethod.GET, "/test", "test");
    IllegalArgumentException illegalArg = new IllegalArgumentException("test");
    Exception generic = new Exception("test");

    // When / Then
    assertThat(handler.handleUnauthorizedException(unauth).getBody().getFailure()).isNotNull();
    assertThat(handler.handleNoResourceFoundException(notFound).getBody().getFailure()).isNotNull();
    assertThat(handler.handleIllegalArgumentException(illegalArg).getBody().getFailure()).isNotNull();
    assertThat(handler.handleGenericException(generic).getBody().getFailure()).isNotNull();
  }
}




