package io.cmartinezs.keygo.api.membership.response;

import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import java.time.OffsetDateTime;
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
) {}

