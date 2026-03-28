package io.cmartinezs.keygo.supabase.platform.adapter;

import io.cmartinezs.keygo.app.platform.port.PlatformStatsPort;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.supabase.auth.repository.SigningKeyJpaRepository;
import io.cmartinezs.keygo.supabase.clientapp.repository.ClientAppJpaRepository;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import io.cmartinezs.keygo.supabase.user.repository.TenantUserJpaRepository;
import org.springframework.stereotype.Component;

/**
 * JPA adapter for PlatformStatsPort.
 * <p>Adaptador JPA para PlatformStatsPort.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Component
public class PlatformStatsAdapter implements PlatformStatsPort {

  private final TenantJpaRepository tenantRepo;
  private final TenantUserJpaRepository userRepo;
  private final ClientAppJpaRepository appRepo;
  private final SigningKeyJpaRepository signingKeyRepo;

  public PlatformStatsAdapter(
      TenantJpaRepository tenantRepo,
      TenantUserJpaRepository userRepo,
      ClientAppJpaRepository appRepo,
      SigningKeyJpaRepository signingKeyRepo) {
    this.tenantRepo = tenantRepo;
    this.userRepo = userRepo;
    this.appRepo = appRepo;
    this.signingKeyRepo = signingKeyRepo;
  }

  @Override
  public long countTotalTenants() {
    return tenantRepo.count();
  }

  @Override
  public long countTenantsByStatus(TenantStatus status) {
    return tenantRepo.countByStatus(status);
  }

  @Override
  public long countTotalUsers() {
    return userRepo.count();
  }

  @Override
  public long countUsersByStatus(UserStatus status) {
    return userRepo.countByStatus(status);
  }

  @Override
  public long countTotalApps() {
    return appRepo.count();
  }

  @Override
  public long countActiveSigningKeys() {
    return signingKeyRepo.countByStatus("ACTIVE");
  }
}

