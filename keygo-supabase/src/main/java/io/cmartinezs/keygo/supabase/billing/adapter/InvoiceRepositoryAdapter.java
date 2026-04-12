package io.cmartinezs.keygo.supabase.billing.adapter;

import io.cmartinezs.keygo.app.billing.invoice.port.InvoiceRepositoryPort;
import io.cmartinezs.keygo.domain.billing.invoice.model.Invoice;
import io.cmartinezs.keygo.supabase.billing.entity.InvoiceEntity;
import io.cmartinezs.keygo.supabase.billing.mapper.BillingPersistenceMapper;
import io.cmartinezs.keygo.supabase.billing.repository.AppSubscriptionJpaRepository;
import io.cmartinezs.keygo.supabase.billing.repository.InvoiceJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter: implements InvoiceRepositoryPort using JPA.
 * @author cmartinezs
 * @version 1.0
 */
@Repository
public class InvoiceRepositoryAdapter implements InvoiceRepositoryPort {

  private final InvoiceJpaRepository jpaRepo;
  private final AppSubscriptionJpaRepository subscriptionRepo;

  public InvoiceRepositoryAdapter(InvoiceJpaRepository jpaRepo, AppSubscriptionJpaRepository subscriptionRepo) {
    this.jpaRepo = jpaRepo;
    this.subscriptionRepo = subscriptionRepo;
  }

  @Override
  public Invoice save(Invoice invoice) {
    var subscription = subscriptionRepo.getReferenceById(invoice.getSubscriptionId());
    InvoiceEntity entity = InvoiceEntity.builder()
        .id(invoice.getId())
        .subscription(subscription)
        .contractor(subscription.getContractor())
        .clientApp(subscription.getClientApp())
        .invoiceNumber(invoice.getInvoiceNumber())
        .status(invoice.getStatus())
        .issueDate(invoice.getIssueDate())
        .dueDate(invoice.getDueDate())
        .periodStart(invoice.getPeriodStart())
        .periodEnd(invoice.getPeriodEnd())
        .currency(invoice.getCurrency())
        .subtotal(invoice.getSubtotal())
        .taxAmount(invoice.getTaxAmount())
        .total(invoice.getTotal())
        .billingNameSnapshot(invoice.getBillingNameSnapshot())
        .billingTaxIdSnapshot(invoice.getBillingTaxIdSnapshot())
        .billingAddressSnapshot(invoice.getBillingAddressSnapshot())
        .planNameSnapshot(invoice.getPlanNameSnapshot())
        .planVersionSnapshot(invoice.getPlanVersionSnapshot())
        .paidAt(null)
        .build();
    return BillingPersistenceMapper.toDomain(jpaRepo.save(entity));
  }

  @Override
  public Optional<Invoice> findById(UUID id) {
    return jpaRepo.findById(id).map(BillingPersistenceMapper::toDomain);
  }

  @Override
  public List<Invoice> findBySubscriptionId(UUID subscriptionId) {
    return jpaRepo.findBySubscriptionId(subscriptionId)
        .stream().map(BillingPersistenceMapper::toDomain).toList();
  }
}

