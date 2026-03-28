package io.cmartinezs.keygo.api.platform.response;

import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import lombok.Builder;
import lombok.Getter;


/**
 * DTO for service information data.
 * DTO para datos de información del servicio.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class ServiceInfoData {
  private String title;
  private String name;
  private String version;
  private String environment;
  private String status;

  /** Solo para referencia de schema OpenAPI — no instanciar en lógica de negocio. */
  public static final class Response extends BaseResponse<ServiceInfoData> {
  }
}
