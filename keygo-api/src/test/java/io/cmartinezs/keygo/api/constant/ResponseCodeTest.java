package io.cmartinezs.keygo.api.constant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ResponseCode enum
 * Pruebas unitarias para ResponseCode enum
 *
 * @author cmartinezs
 * @version 1.0
 */
class ResponseCodeTest {

    @Test
    void serviceInfoRetrieved_shouldHaveCorrectCodeAndMessage() {
        // When
        ResponseCode code = ResponseCode.SERVICE_INFO_RETRIEVED;

        // Then
        assertThat(code.getCode()).isEqualTo("SERVICE_INFO_RETRIEVED");
        assertThat(code.getMessage()).isEqualTo("Service information retrieved successfully");
    }

    @Test
    void resourceCreated_shouldHaveCorrectCodeAndMessage() {
        // When
        ResponseCode code = ResponseCode.RESOURCE_CREATED;

        // Then
        assertThat(code.getCode()).isEqualTo("RESOURCE_CREATED");
        assertThat(code.getMessage()).isEqualTo("Resource created successfully");
    }

    @Test
    void resourceNotFound_shouldHaveCorrectCodeAndMessage() {
        // When
        ResponseCode code = ResponseCode.RESOURCE_NOT_FOUND;

        // Then
        assertThat(code.getCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(code.getMessage()).isEqualTo("Requested resource was not found");
    }

    @Test
    void invalidInput_shouldHaveCorrectCodeAndMessage() {
        // When
        ResponseCode code = ResponseCode.INVALID_INPUT;

        // Then
        assertThat(code.getCode()).isEqualTo("INVALID_INPUT");
        assertThat(code.getMessage()).isEqualTo("Invalid input data provided");
    }

    @Test
    void authenticationRequired_shouldHaveCorrectCodeAndMessage() {
        // When
        ResponseCode code = ResponseCode.AUTHENTICATION_REQUIRED;

        // Then
        assertThat(code.getCode()).isEqualTo("AUTHENTICATION_REQUIRED");
        assertThat(code.getMessage()).isEqualTo("Authentication is required");
    }

    @Test
    void allResponseCodes_shouldHaveNonNullCodeAndMessage() {
        // When / Then
        for (ResponseCode code : ResponseCode.values()) {
            assertThat(code.getCode()).isNotNull().isNotEmpty();
            assertThat(code.getMessage()).isNotNull().isNotEmpty();
        }
    }

    @Test
    void allResponseCodes_shouldHaveUniqueCode() {
        // When
        ResponseCode[] codes = ResponseCode.values();

        // Then
        long uniqueCount = java.util.Arrays.stream(codes)
                .map(ResponseCode::getCode)
                .distinct()
                .count();

        assertThat(uniqueCount).isEqualTo(codes.length);
    }
}

