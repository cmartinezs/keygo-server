package io.cmartinezs.keygo.app.billing.invoice.usecase;

import io.cmartinezs.keygo.app.billing.invoice.port.InvoiceRepositoryPort;
import io.cmartinezs.keygo.domain.billing.invoice.model.Invoice;

import java.util.List;
import java.util.UUID;

/**
 * Use case: list all invoices for a subscription.
 * @author cmartinezs
 * @version 1.0
 */
public class ListAppInvoicesUseCase {

  private final InvoiceRepositoryPort invoiceRepo;

  public ListAppInvoicesUseCase(InvoiceRepositoryPort invoiceRepo) {
    this.invoiceRepo = invoiceRepo;
  }

  public List<Invoice> execute(UUID subscriptionId) {
    return invoiceRepo.findBySubscriptionId(subscriptionId);
  }
}

