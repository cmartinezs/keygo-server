package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, UUID> {
  List<InvoiceEntity> findBySubscriptionId(UUID subscriptionId);
}

