package io.cmartinezs.keygo.app.billing.invoice.port;

import io.cmartinezs.keygo.domain.billing.invoice.model.Invoice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port OUT — persistence contract for Invoice.
 * @author cmartinezs
 * @version 1.0
 */
public interface InvoiceRepositoryPort {
  Invoice save(Invoice invoice);
  Optional<Invoice> findById(UUID id);
  List<Invoice> findBySubscriptionId(UUID subscriptionId);
}

