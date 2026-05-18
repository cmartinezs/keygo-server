package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.membership.result.AppAccessResult;
import io.cmartinezs.keygo.app.membership.result.TenantAccessResult;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case: return all tenants and apps a platform-authenticated user has access to.
 *
 * <p>Derives everything from the authenticated user's ID (JWT {@code sub}) — no tenant scope
 * required. Result is grouped by tenant, then by app membership.
 *
 * <p>Used by: {@code GET /api/v1/platform/account/access}
 *
 * @author cmartinezs
 * @version 1.0
 */
public class GetAccountAccessUseCase {

  private final MembershipRepositoryPort membershipRepository;
  private final ClientAppRepositoryPort clientAppRepository;
  private final TenantRepositoryPort tenantRepository;

  public GetAccountAccessUseCase(
      MembershipRepositoryPort membershipRepository,
      ClientAppRepositoryPort clientAppRepository,
      TenantRepositoryPort tenantRepository) {
    this.membershipRepository = membershipRepository;
    this.clientAppRepository = clientAppRepository;
    this.tenantRepository = tenantRepository;
  }

  /**
   * Returns the access view for the given platform user.
   *
   * @param userId the authenticated platform user's UUID (from JWT {@code sub})
   * @return list of tenant access entries; empty if the user has no memberships
   */
  public List<TenantAccessResult> execute(UUID userId) {
    List<Membership> memberships = membershipRepository.findByUserId(userId);

    Map<UUID, TenantEntry> byTenant = new LinkedHashMap<>();

    for (Membership membership : memberships) {
      UUID clientAppUuid = membership.getClientAppId().value();

      Optional<ClientApp> appOpt = clientAppRepository.findById(ClientAppId.of(clientAppUuid));
      if (appOpt.isEmpty()) {
        continue;
      }
      ClientApp app = appOpt.get();

      UUID tenantUuid = app.getTenantId().value();
      Optional<Tenant> tenantOpt = tenantRepository.findById(TenantId.of(tenantUuid));
      if (tenantOpt.isEmpty()) {
        continue;
      }
      Tenant tenant = tenantOpt.get();

      List<String> roles = membershipRepository.findRoleCodesByUserAndClientApp(userId, clientAppUuid);

      AppAccessResult appEntry = new AppAccessResult(
          membership.getId().value(),
          membership.getStatus().name(),
          clientAppUuid,
          app.getClientId().value(),
          app.getName(),
          roles);

      byTenant.computeIfAbsent(tenantUuid, id -> new TenantEntry(tenant)).apps.add(appEntry);
    }

    return byTenant.values().stream()
        .map(TenantEntry::toResult)
        .toList();
  }

  private static final class TenantEntry {
    final Tenant tenant;
    final List<AppAccessResult> apps = new ArrayList<>();

    TenantEntry(Tenant tenant) {
      this.tenant = tenant;
    }

    TenantAccessResult toResult() {
      return new TenantAccessResult(
          tenant.getId().value(),
          tenant.getSlug().value(),
          tenant.getName(),
          List.copyOf(apps));
    }
  }
}
