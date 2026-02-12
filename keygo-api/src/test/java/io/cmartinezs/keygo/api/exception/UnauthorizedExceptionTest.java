package io.cmartinezs.keygo.api.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UnauthorizedException
 * Pruebas unitarias para UnauthorizedException
 *
 * @author cmartinezs
 * @version 1.0
 */
class UnauthorizedExceptionTest {

  @Test
  void constructor_shouldSetMessage() {
    // Given
    String message = "Test unauthorized message";

    // When
    UnauthorizedException exception = new UnauthorizedException(message);

    // Then
    assertThat(exception.getMessage()).isEqualTo(message);
  }

  @Test
  void constructor_shouldSetMessageAndCause() {
    // Given
    String message = "Test unauthorized message";
    Throwable cause = new RuntimeException("Cause");

    // When
    UnauthorizedException exception = new UnauthorizedException(message, cause);

    // Then
    assertThat(exception.getMessage()).isEqualTo(message);
    assertThat(exception.getCause()).isEqualTo(cause);
  }

  @Test
  void annotation_shouldHaveUnauthorizedStatus() {
    // When
    ResponseStatus annotation = UnauthorizedException.class.getAnnotation(ResponseStatus.class);

    // Then
    assertThat(annotation).isNotNull();
    assertThat(annotation.value()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void exception_shouldBeRuntimeException() {
    // Given
    UnauthorizedException exception = new UnauthorizedException("Test");

    // When / Then
    assertThat(exception).isInstanceOf(RuntimeException.class);
  }
}

