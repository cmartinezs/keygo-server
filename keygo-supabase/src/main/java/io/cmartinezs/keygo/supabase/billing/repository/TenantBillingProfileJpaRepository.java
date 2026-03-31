package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.TenantBillingProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for tenant_billing_profiles table.
 * @author cmartinezs
 * @version 1.0
 */
public interface TenantBillingProfileJpaRepository extends JpaRepository<TenantBillingProfileEntity, UUID> {
  List<TenantBillingProfileEntity> findByTenantId(UUID tenantId);
  Optional<TenantBillingProfileEntity> findByTenantIdAndIsDefaultTrue(UUID tenantId);
}

