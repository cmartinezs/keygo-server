package io.cmartinezs.keygo.supabase.billing.repository;

import io.cmartinezs.keygo.supabase.billing.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for payment_transactions table.
 * @author cmartinezs
 * @version 1.0
 */
public interface PaymentTransactionJpaRepository extends JpaRepository<PaymentTransactionEntity, UUID> {
  List<PaymentTransactionEntity> findByContractId(UUID contractId);
  List<PaymentTransactionEntity> findBySubscriptionId(UUID subscriptionId);
}

