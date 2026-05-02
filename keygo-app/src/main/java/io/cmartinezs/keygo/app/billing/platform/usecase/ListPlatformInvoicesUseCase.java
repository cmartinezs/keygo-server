package io.cmartinezs.keygo.app.billing.platform.usecase;

import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.invoice.port.InvoiceRepositoryPort;
import io.cmartinezs.keygo.app.billing.platform.exception.ContractorNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.exception.SubscriptionNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.invoice.model.Invoice;

import java.util.List;
import java.util.UUID;

/**
 * Use case: list invoices for the platform subscription of a contractor.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ListPlatformInvoicesUseCase {

  private final ContractorRepositoryPort contractorRepo;
  private final AppSubscriptionRepositoryPort subscriptionRepo;
  private final InvoiceRepositoryPort invoiceRepo;

  public ListPlatformInvoicesUseCase(
      ContractorRepositoryPort contractorRepo,
      AppSubscriptionRepositoryPort subscriptionRepo,
      InvoiceRepositoryPort invoiceRepo) {
    this.contractorRepo = contractorRepo;
    this.subscriptionRepo = subscriptionRepo;
    this.invoiceRepo = invoiceRepo;
  }

  public List<Invoice> execute(UUID platformUserId) {
    var contractor = contractorRepo.findByPlatformUserId(platformUserId)
        .orElseThrow(() -> new ContractorNotFoundException(platformUserId));

    var subscription = subscriptionRepo.findPlatformSubscriptionByContractorId(contractor.getId())
        .orElseThrow(() -> new SubscriptionNotFoundException("contractorId", contractor.getId().toString()));

    return invoiceRepo.findBySubscriptionId(subscription.getId());
  }
}
