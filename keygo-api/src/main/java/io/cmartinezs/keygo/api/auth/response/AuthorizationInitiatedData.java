package io.cmartinezs.keygo.api.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;


/**
 * Response: Autorización iniciada con datos de la app cliente.
 *
 * @param clientId ID del cliente
 * @param clientName nombre de la app
 * @param redirectUri URI de redirección
 */
public record AuthorizationInitiatedData(
    @JsonProperty("client_id") String clientId,
    @JsonProperty("client_name") String clientName,
    @JsonProperty("redirect_uri") String redirectUri) {

  /** Solo para referencia de schema OpenAPI — no instanciar en lógica de negocio. */
  public static final class Response extends BaseResponse<AuthorizationInitiatedData> {
  }
}
