package io.cmartinezs.keygo.app.membership.result;

import java.util.List;
import java.util.UUID;

/**
 * Tenant access entry for a platform account user — groups all app memberships under a tenant.
 *
 * @param tenantId   UUID of the tenant
 * @param tenantSlug URL-friendly tenant identifier
 * @param tenantName display name of the tenant
 * @param apps       list of app access entries for this tenant
 */
public record TenantAccessResult(
    UUID tenantId,
    String tenantSlug,
    String tenantName,
    List<AppAccessResult> apps) {}
