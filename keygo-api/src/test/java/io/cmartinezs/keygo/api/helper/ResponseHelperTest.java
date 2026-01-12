package io.cmartinezs.keygo.api.helper;

import io.cmartinezs.keygo.api.constant.ResponseCode;
import io.cmartinezs.keygo.api.dto.reponse.MessageResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ResponseHelper
 * Pruebas unitarias para ResponseHelper
 *
 * @author cmartinezs
 * @version 1.0
 */
class ResponseHelperTest {

    @Test
    void message_withResponseCode_shouldCreateMessageWithDefaultMessage() {
        // Given
        ResponseCode responseCode = ResponseCode.RESOURCE_RETRIEVED;

        // When
        MessageResponse result = ResponseHelper.message(responseCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("RESOURCE_RETRIEVED");
        assertThat(result.getMessage()).isEqualTo("Resource retrieved successfully");
    }

    @Test
    void message_withResponseCodeAndCustomMessage_shouldCreateMessageWithCustomMessage() {
        // Given
        ResponseCode responseCode = ResponseCode.RESOURCE_CREATED;
        String customMessage = "User created with ID: 123";

        // When
        MessageResponse result = ResponseHelper.message(responseCode, customMessage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("RESOURCE_CREATED");
        assertThat(result.getMessage()).isEqualTo(customMessage);
    }

    @Test
    void message_withStringCodeAndMessage_shouldCreateMessage() {
        // Given
        String code = "CUSTOM_CODE";
        String message = "Custom message";

        // When
        MessageResponse result = ResponseHelper.message(code, message);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo(code);
        assertThat(result.getMessage()).isEqualTo(message);
    }

    @Test
    void message_withServiceInfoRetrieved_shouldReturnCorrectMessage() {
        // Given
        ResponseCode responseCode = ResponseCode.SERVICE_INFO_RETRIEVED;

        // When
        MessageResponse result = ResponseHelper.message(responseCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("SERVICE_INFO_RETRIEVED");
        assertThat(result.getMessage()).contains("Service information");
    }

    @Test
    void message_withErrorCode_shouldReturnErrorMessage() {
        // Given
        ResponseCode responseCode = ResponseCode.RESOURCE_NOT_FOUND;

        // When
        MessageResponse result = ResponseHelper.message(responseCode);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(result.getMessage()).contains("not found");
    }
}

