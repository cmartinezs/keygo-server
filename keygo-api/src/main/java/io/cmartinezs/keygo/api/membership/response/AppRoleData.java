package io.cmartinezs.keygo.api.membership.response;

import io.cmartinezs.keygo.api.shared.response.BaseResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * Response DTO for app role data.
 * <p>DTO de response para datos de rol de app.
 * @author cmartinezs
 * @version 1.0
 */
@Builder
public record AppRoleData(
    UUID id,
    UUID clientAppId,
    String code,
    String displayName,
    String description,
    OffsetDateTime createdAt
) {

  /** Solo para referencia de schema OpenAPI — no instanciar en lógica de negocio. */
  public static final class Response extends BaseResponse<AppRoleData> {
  }

  /** Solo para referencia de schema OpenAPI (lista). */
  public static final class ListResponse extends BaseResponse<List<AppRoleData>> {
  }
}
