package io.cmartinezs.keygo.api.controller;

import static org.assertj.core.api.Assertions.assertThat;

import io.cmartinezs.keygo.api.constant.ResponseCode;
import io.cmartinezs.keygo.api.dto.reponse.BaseResponse;
import io.cmartinezs.keygo.api.dto.reponse.MessageResponse;
import io.cmartinezs.keygo.api.dto.reponse.ResponseCodeCatalog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for ResponseCodeController Pruebas unitarias para ResponseCodeController
 *
 * @author cmartinezs
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class ResponseCodeControllerTest {

  @InjectMocks private ResponseCodeController controller;

  @Test
  void getResponseCodeCatalog_shouldReturnCatalog() {
    // When
    ResponseEntity<BaseResponse<ResponseCodeCatalog>> response =
        controller.getResponseCodeCatalog();

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    BaseResponse<ResponseCodeCatalog> body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.getData()).isNotNull();
  }

  @Test
  void getResponseCodeCatalog_shouldReturnSuccessAndFailureCodes() {
    // When
    ResponseEntity<BaseResponse<ResponseCodeCatalog>> response =
        controller.getResponseCodeCatalog();

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getBody()).isNotNull();

    ResponseCodeCatalog catalog = response.getBody().getData();
    assertThat(catalog).isNotNull();
    assertThat(catalog.getSuccessCodes()).isNotEmpty();
    assertThat(catalog.getFailureCodes()).isNotEmpty();
  }

  @Test
  void getResponseCodeCatalog_shouldContainAllResponseCodes() {
    // When
    ResponseEntity<BaseResponse<ResponseCodeCatalog>> response =
        controller.getResponseCodeCatalog();

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getBody()).isNotNull();

    ResponseCodeCatalog catalog = response.getBody().getData();
    assertThat(catalog).isNotNull();

    int totalCodes = catalog.getSuccessCodes().size() + catalog.getFailureCodes().size();
    assertThat(totalCodes).isEqualTo(ResponseCode.values().length);
  }

  @Test
  void getResponseCodeCatalog_shouldHaveCorrectSuccessMessage() {
    // When
    ResponseEntity<BaseResponse<ResponseCodeCatalog>> response =
        controller.getResponseCodeCatalog();

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getBody()).isNotNull();

    MessageResponse success = response.getBody().getSuccess();
    assertThat(success).isNotNull();
    assertThat(success.getCode()).isEqualTo(ResponseCode.RESPONSE_CODES_RETRIEVED.getCode());
  }

  @Test
  void getResponseCodeCatalog_successCodes_shouldContainRetrievedCodes() {
    // When
    ResponseEntity<BaseResponse<ResponseCodeCatalog>> response =
        controller.getResponseCodeCatalog();

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getBody()).isNotNull();

    ResponseCodeCatalog catalog = response.getBody().getData();
    assertThat(catalog).isNotNull();

    boolean hasRetrievedCode =
        catalog.getSuccessCodes().stream().anyMatch(code -> code.getCode().contains("RETRIEVED"));
    assertThat(hasRetrievedCode).isTrue();
  }

  @Test
  void getResponseCodeCatalog_failureCodes_shouldContainErrorCodes() {
    // When
    ResponseEntity<BaseResponse<ResponseCodeCatalog>> response =
        controller.getResponseCodeCatalog();

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getBody()).isNotNull();

    ResponseCodeCatalog catalog = response.getBody().getData();
    assertThat(catalog).isNotNull();

    boolean hasErrorCode =
        catalog.getFailureCodes().stream()
            .anyMatch(
                code -> code.getCode().contains("NOT_FOUND") || code.getCode().contains("INVALID"));
    assertThat(hasErrorCode).isTrue();
  }

  @Test
  void getResponseCodeCatalog_allCodes_shouldHaveCodeAndMessage() {
    // When
    ResponseEntity<BaseResponse<ResponseCodeCatalog>> response =
        controller.getResponseCodeCatalog();

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getBody()).isNotNull();

    ResponseCodeCatalog catalog = response.getBody().getData();
    assertThat(catalog).isNotNull();

    catalog
        .getSuccessCodes()
        .forEach(
            code -> {
              assertThat(code.getCode()).isNotNull().isNotEmpty();
              assertThat(code.getMessage()).isNotNull().isNotEmpty();
              assertThat(code.getType()).isEqualTo("SUCCESS");
            });

    catalog
        .getFailureCodes()
        .forEach(
            code -> {
              assertThat(code.getCode()).isNotNull().isNotEmpty();
              assertThat(code.getMessage()).isNotNull().isNotEmpty();
              assertThat(code.getType()).isEqualTo("FAILURE");
            });
  }
}
