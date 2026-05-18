package io.cmartinezs.keygo.api.membership.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * Response DTO for membership data.
 * <p>DTO de response para datos de membresía.
 * @author cmartinezs
 * @version 1.2
 */
@Builder
public record MembershipData(
    UUID id,
    @JsonProperty("user_id") UUID userId,
    @JsonProperty("client_app_id") UUID clientAppId,
    MembershipStatus status,
    List<MembershipRoleData> roles,
    @JsonProperty("created_at") OffsetDateTime createdAt,
    @JsonProperty("notification_email") String notificationEmail
) {

}
