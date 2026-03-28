package io.cmartinezs.keygo.api.membership.response;

import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;

/**
 * Response DTO for membership data.
 * <p>DTO de response para datos de membresía.
 * @author cmartinezs
 * @version 1.0
 */
@Builder
public record MembershipData(
    UUID id,
    UUID userId,
    UUID clientAppId,
    MembershipStatus status,
    Set<UUID> roleIds,
    OffsetDateTime createdAt
) {

  /** Solo para referencia de schema OpenAPI — no instanciar en lógica de negocio. */
  public static final class Response extends BaseResponse<MembershipData> {
  }

  /** Solo para referencia de schema OpenAPI (lista). */
  public static final class ListResponse extends BaseResponse<List<MembershipData>> {
  }
}
