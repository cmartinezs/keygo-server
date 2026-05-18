package io.cmartinezs.keygo.api.membership.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * Response DTO for a tenant access entry within a platform account access view.
 * Groups all app memberships the authenticated user holds in a given tenant.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Builder
public record TenantAccessData(
    @JsonProperty("tenant_id") UUID tenantId,
    @JsonProperty("tenant_slug") String tenantSlug,
    @JsonProperty("tenant_name") String tenantName,
    List<AppAccessData> apps) {}
