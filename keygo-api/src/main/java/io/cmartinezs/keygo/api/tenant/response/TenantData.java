package io.cmartinezs.keygo.api.tenant.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Response DTO representing a tenant in API responses.
 * DTO de respuesta que representa un tenant en las respuestas de la API.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class TenantData {

  private final String id;
  private final String name;
  private final String slug;
  private final String ownerEmail;
  private final String status;
}

