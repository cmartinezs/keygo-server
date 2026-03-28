package io.cmartinezs.keygo.api.clientapp.response;

import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Response DTO returned when a client secret is created or rotated.
 * Contains the raw secret — available only once.
 * <p>DTO de respuesta devuelto cuando se crea o rota el secret de un cliente.
 * El secret en texto plano solo está disponible una vez.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class ClientAppSecretData {

  /* The OAuth2 client_id */
  private String clientId;

  /* The raw client secret — store it immediately, it will not be shown again */
  private String clientSecret;

  /** Solo para referencia de schema OpenAPI — no instanciar en lógica de negocio. */
  public static final class Response extends BaseResponse<ClientAppSecretData> {}
}
