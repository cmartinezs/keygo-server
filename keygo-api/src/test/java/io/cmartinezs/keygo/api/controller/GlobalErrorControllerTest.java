package io.cmartinezs.keygo.api.controller;

import io.cmartinezs.keygo.api.constant.ResponseCode;
import io.cmartinezs.keygo.api.dto.reponse.BaseResponse;
import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GlobalErrorController
 * Pruebas unitarias para GlobalErrorController
 *
 * @author cmartinezs
 * @version 1.0
 */
class GlobalErrorControllerTest {

  private GlobalErrorController controller;
  private MockHttpServletRequest request;

  @BeforeEach
  void setUp() {
    controller = new GlobalErrorController();
    request = new MockHttpServletRequest();
  }

  @Test
  void handleError_shouldReturnUnauthorizedForStatus401() {
    // Given
    request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 401);
    request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/api/test");
    request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Unauthorized");

    // When
    ResponseEntity<BaseResponse<Void>> response = controller.handleError(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.AUTHENTICATION_REQUIRED.getCode());
  }

  @Test
  void handleError_shouldReturnNotFoundForStatus404() {
    // Given
    request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 404);
    request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/api/unknown");
    request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Not Found");

    // When
    ResponseEntity<BaseResponse<Void>> response = controller.handleError(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.RESOURCE_NOT_FOUND.getCode());
  }

  @Test
  void handleError_shouldReturnBadRequestForStatus400() {
    // Given
    request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 400);
    request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/api/test");
    request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Bad Request");

    // When
    ResponseEntity<BaseResponse<Void>> response = controller.handleError(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.INVALID_INPUT.getCode());
  }

  @Test
  void handleError_shouldReturnForbiddenForStatus403() {
    // Given
    request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 403);
    request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/api/test");
    request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Forbidden");

    // When
    ResponseEntity<BaseResponse<Void>> response = controller.handleError(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.INSUFFICIENT_PERMISSIONS.getCode());
  }

  @Test
  void handleError_shouldReturnInternalServerErrorWhenNoStatusCode() {
    // Given
    request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/api/test");
    request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "Unknown error");

    // When
    ResponseEntity<BaseResponse<Void>> response = controller.handleError(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.OPERATION_FAILED.getCode());
  }

  @Test
  void handleError_shouldReturnOperationFailedForUnmappedStatus() {
    // Given
    request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 502);
    request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/api/test");

    // When
    ResponseEntity<BaseResponse<Void>> response = controller.handleError(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getFailure()).isNotNull();
    assertThat(response.getBody().getFailure().getCode())
        .isEqualTo(ResponseCode.OPERATION_FAILED.getCode());
  }

  @Test
  void handleError_shouldHandleException() {
    // Given
    Exception exception = new RuntimeException("Test exception");
    request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 500);
    request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/api/test");
    request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, exception);

    // When
    ResponseEntity<BaseResponse<Void>> response = controller.handleError(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody()).isNotNull();
  }
}

