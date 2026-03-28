package io.cmartinezs.keygo.api.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;


/**
 * Response: Código de autorización emitido.
 *
 * @param code valor del código
 * @param redirectUri URI donde redirigir
 */
public record AuthorizationCodeData(
    @JsonProperty("code") String code,
    @JsonProperty("redirect_uri") String redirectUri) {

  /** Solo para referencia de schema OpenAPI — no instanciar en lógica de negocio. */
  public static final class Response extends BaseResponse<AuthorizationCodeData> {
  }
}
