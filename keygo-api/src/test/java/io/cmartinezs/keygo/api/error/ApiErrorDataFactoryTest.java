package io.cmartinezs.keygo.api.error;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorDataFactoryTest {

  @Test
  void fromDetail_shouldClassifyClientRequestErrors() {
    // Given
    ResponseCode responseCode = ResponseCode.INVALID_INPUT;

    // When
    ErrorData data = ApiErrorDataFactory.fromDetail(responseCode, null, null, false);

    // Then
    assertThat(data.getOrigin()).isEqualTo(ApiErrorOrigin.CLIENT_REQUEST);
    assertThat(data.getClientRequestCause()).isEqualTo(ApiClientRequestCause.USER_INPUT);
  }

  @Test
  void fromDetail_shouldClassifyBusinessRuleErrors() {
    // Given
    ResponseCode responseCode = ResponseCode.BUSINESS_RULE_VIOLATION;

    // When
    ErrorData data = ApiErrorDataFactory.fromDetail(responseCode, null, null, false);

    // Then
    assertThat(data.getOrigin()).isEqualTo(ApiErrorOrigin.BUSINESS_RULE);
    assertThat(data.getClientRequestCause()).isNull();
  }

  @Test
  void fromDetail_shouldClassifyServerProcessingErrorsByDefault() {
    // Given
    ResponseCode responseCode = ResponseCode.OPERATION_FAILED;

    // When
    ErrorData data = ApiErrorDataFactory.fromDetail(responseCode, null, null, false);

    // Then
    assertThat(data.getOrigin()).isEqualTo(ApiErrorOrigin.SERVER_PROCESSING);
    assertThat(data.getClientRequestCause()).isNull();
  }

  @Test
  void fromException_shouldClassifyInvalidCredentialsAsUserInput() {
    // Given
    InvalidCredentialsException exception = new InvalidCredentialsException();

    // When
    ErrorData data =
        ApiErrorDataFactory.fromException(
            ResponseCode.AUTHENTICATION_REQUIRED,
            exception,
            false);

    // Then
    assertThat(data.getOrigin()).isEqualTo(ApiErrorOrigin.CLIENT_REQUEST);
    assertThat(data.getClientRequestCause()).isEqualTo(ApiClientRequestCause.USER_INPUT);
  }

  @Test
  void fromException_shouldClassifyMissingParameterAsClientTechnical() {
    // Given
    MissingServletRequestParameterException exception =
        new MissingServletRequestParameterException("scope", "String");

    // When
    ErrorData data =
        ApiErrorDataFactory.fromException(
            ResponseCode.INVALID_INPUT,
            exception,
            false);

    // Then
    assertThat(data.getOrigin()).isEqualTo(ApiErrorOrigin.CLIENT_REQUEST);
    assertThat(data.getClientRequestCause()).isEqualTo(ApiClientRequestCause.CLIENT_TECHNICAL);
  }
}

