package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.PaymentMethodEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for payment_methods table.
 * @author cmartinezs
 * @version 1.0
 */
public interface PaymentMethodJpaRepository extends JpaRepository<PaymentMethodEntity, UUID> {
  List<PaymentMethodEntity> findByTenantIdAndStatus(UUID tenantId, String status);
  java.util.Optional<PaymentMethodEntity> findByTenantIdAndIsDefaultTrue(UUID tenantId);
}

