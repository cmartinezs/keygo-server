package io.cmartinezs.keygo.api.billing.request;

/**
 * Request body for the verify-email endpoint in the contracting flow.
 */
public record VerifyContractEmailRequest(String code) {}

