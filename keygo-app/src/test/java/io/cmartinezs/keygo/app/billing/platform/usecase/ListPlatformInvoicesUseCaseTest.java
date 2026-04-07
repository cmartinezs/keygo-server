package io.cmartinezs.keygo.app.billing.platform.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.invoice.port.InvoiceRepositoryPort;
import io.cmartinezs.keygo.app.billing.platform.exception.ContractorNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.exception.SubscriptionNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.billing.invoice.model.Invoice;
import io.cmartinezs.keygo.domain.billing.invoice.model.InvoiceStatus;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriptionStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListPlatformInvoicesUseCase")
class ListPlatformInvoicesUseCaseTest {

  @Mock private ContractorRepositoryPort contractorRepo;
  @Mock private AppSubscriptionRepositoryPort subscriptionRepo;
  @Mock private InvoiceRepositoryPort invoiceRepo;

  private ListPlatformInvoicesUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ListPlatformInvoicesUseCase(contractorRepo, subscriptionRepo, invoiceRepo);
  }

  @Test
  @DisplayName("Debe retornar facturas del contractor")
  void execute_returnsInvoicesForContractor() {
    // Given
    UUID platformUserId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();
    UUID subscriptionId = UUID.randomUUID();

    Contractor contractor = Contractor.builder()
        .id(contractorId).platformUserId(platformUserId)
        .status(ContractorStatus.ACTIVE).build();
    when(contractorRepo.findByPlatformUserId(platformUserId)).thenReturn(Optional.of(contractor));

    AppSubscription subscription = AppSubscription.builder()
        .id(subscriptionId).contractorId(contractorId)
        .appPlanVersionId(UUID.randomUUID())
        .status(SubscriptionStatus.ACTIVE)
        .currentPeriodStart(OffsetDateTime.now().minusDays(30))
        .currentPeriodEnd(OffsetDateTime.now())
        .autoRenew(true)
        .createdAt(OffsetDateTime.now()).build();
    when(subscriptionRepo.findPlatformSubscriptionByContractorId(contractorId))
        .thenReturn(Optional.of(subscription));

    Invoice invoice = Invoice.builder()
        .id(UUID.randomUUID()).subscriptionId(subscriptionId)
        .invoiceNumber("INV-001")
        .status(InvoiceStatus.PAID)
        .issueDate(LocalDate.now().minusDays(30))
        .dueDate(LocalDate.now().minusDays(15))
        .periodStart(LocalDate.now().minusDays(30))
        .periodEnd(LocalDate.now())
        .currency("USD")
        .subtotal(BigDecimal.valueOf(9.99))
        .taxAmount(BigDecimal.ZERO)
        .total(BigDecimal.valueOf(9.99))
        .createdAt(OffsetDateTime.now()).build();
    when(invoiceRepo.findBySubscriptionId(subscriptionId)).thenReturn(List.of(invoice));

    // When
    List<Invoice> result = useCase.execute(platformUserId);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getInvoiceNumber()).isEqualTo("INV-001");
    verify(invoiceRepo).findBySubscriptionId(subscriptionId);
  }

  @Test
  @DisplayName("Debe retornar lista vacía si no hay facturas")
  void execute_returnsEmptyListWhenNoInvoices() {
    // Given
    UUID platformUserId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();
    UUID subscriptionId = UUID.randomUUID();

    Contractor contractor = Contractor.builder()
        .id(contractorId).platformUserId(platformUserId)
        .status(ContractorStatus.ACTIVE).build();
    when(contractorRepo.findByPlatformUserId(platformUserId)).thenReturn(Optional.of(contractor));

    AppSubscription subscription = AppSubscription.builder()
        .id(subscriptionId).contractorId(contractorId)
        .appPlanVersionId(UUID.randomUUID())
        .status(SubscriptionStatus.ACTIVE)
        .currentPeriodStart(OffsetDateTime.now().minusDays(30))
        .currentPeriodEnd(OffsetDateTime.now())
        .autoRenew(true)
        .createdAt(OffsetDateTime.now()).build();
    when(subscriptionRepo.findPlatformSubscriptionByContractorId(contractorId))
        .thenReturn(Optional.of(subscription));
    when(invoiceRepo.findBySubscriptionId(subscriptionId)).thenReturn(List.of());

    // When
    List<Invoice> result = useCase.execute(platformUserId);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Debe lanzar ContractorNotFoundException si no hay contractor")
  void execute_throwsWhenContractorNotFound() {
    // Given
    UUID platformUserId = UUID.randomUUID();
    when(contractorRepo.findByPlatformUserId(platformUserId)).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(platformUserId))
        .isInstanceOf(ContractorNotFoundException.class);
    verify(subscriptionRepo, never()).findPlatformSubscriptionByContractorId(any());
  }

  @Test
  @DisplayName("Debe lanzar SubscriptionNotFoundException si no hay suscripción")
  void execute_throwsWhenSubscriptionNotFound() {
    // Given
    UUID platformUserId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();

    Contractor contractor = Contractor.builder()
        .id(contractorId).platformUserId(platformUserId)
        .status(ContractorStatus.ACTIVE).build();
    when(contractorRepo.findByPlatformUserId(platformUserId)).thenReturn(Optional.of(contractor));
    when(subscriptionRepo.findPlatformSubscriptionByContractorId(contractorId))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(platformUserId))
        .isInstanceOf(SubscriptionNotFoundException.class);
    verify(invoiceRepo, never()).findBySubscriptionId(any());
  }
}
