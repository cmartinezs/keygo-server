package io.cmartinezs.keygo.api.billing.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for the verify-email endpoint in the contracting flow.
 */
public record VerifyContractEmailRequest(
    @NotBlank(message = "code is required") String code) {}

