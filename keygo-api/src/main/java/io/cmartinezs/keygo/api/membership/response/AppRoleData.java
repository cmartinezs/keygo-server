package io.cmartinezs.keygo.api.membership.response;

import java.time.OffsetDateTime;
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
) {}

