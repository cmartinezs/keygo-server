package io.cmartinezs.keygo.app.billing.platform.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.platform.exception.ContractorNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.exception.SubscriptionInvalidStateException;
import io.cmartinezs.keygo.app.billing.subscription.exception.SubscriptionNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorType;
import io.cmartinezs.keygo.domain.billing.subscription.model.AppSubscription;
import io.cmartinezs.keygo.domain.billing.subscription.model.SubscriptionStatus;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelPlatformSubscriptionUseCase")
class CancelPlatformSubscriptionUseCaseTest {

  @Mock private ContractorRepositoryPort contractorRepo;
  @Mock private AppSubscriptionRepositoryPort subscriptionRepo;

  private CancelPlatformSubscriptionUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new CancelPlatformSubscriptionUseCase(contractorRepo, subscriptionRepo);
  }

  @Test
  @DisplayName("Debe marcar cancelación al fin del período cuando la suscripción está activa")
  void execute_marksCancelAtPeriodEndWhenActive() {
    // Given
    UUID platformUserId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();

    Contractor contractor = Contractor.builder()
        .id(contractorId).primaryContactPlatformUserId(platformUserId)
        .type(ContractorType.PERSON)
        .displayName("Active Contractor")
        .billingEmail("billing@example.com")
        .status(ContractorStatus.ACTIVE).build();
    when(contractorRepo.findByPlatformUserId(platformUserId)).thenReturn(Optional.of(contractor));

    AppSubscription subscription = AppSubscription.builder()
        .id(UUID.randomUUID()).contractorId(contractorId)
        .appPlanVersionId(UUID.randomUUID())
        .status(SubscriptionStatus.ACTIVE)
        .currentPeriodStart(OffsetDateTime.now().minusDays(15))
        .currentPeriodEnd(OffsetDateTime.now().plusDays(15))
        .autoRenew(true)
        .createdAt(OffsetDateTime.now()).build();
    when(subscriptionRepo.findPlatformSubscriptionByContractorId(contractorId))
        .thenReturn(Optional.of(subscription));
    when(subscriptionRepo.save(any(AppSubscription.class))).thenAnswer(inv -> inv.getArgument(0));

    // When
    AppSubscription result = useCase.execute(platformUserId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.isCancelAtPeriodEnd()).isTrue();
    verify(subscriptionRepo).save(any(AppSubscription.class));
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
        .id(contractorId).primaryContactPlatformUserId(platformUserId)
        .type(ContractorType.PERSON)
        .displayName("Active Contractor")
        .billingEmail("billing@example.com")
        .status(ContractorStatus.ACTIVE).build();
    when(contractorRepo.findByPlatformUserId(platformUserId)).thenReturn(Optional.of(contractor));
    when(subscriptionRepo.findPlatformSubscriptionByContractorId(contractorId))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(platformUserId))
        .isInstanceOf(SubscriptionNotFoundException.class);
    verify(subscriptionRepo, never()).save(any());
  }

  @Test
  @DisplayName("Debe lanzar SubscriptionInvalidStateException si la suscripción no está activa")
  void execute_throwsWhenSubscriptionNotActive() {
    // Given
    UUID platformUserId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();

    Contractor contractor = Contractor.builder()
        .id(contractorId).primaryContactPlatformUserId(platformUserId)
        .type(ContractorType.PERSON)
        .displayName("Active Contractor")
        .billingEmail("billing@example.com")
        .status(ContractorStatus.ACTIVE).build();
    when(contractorRepo.findByPlatformUserId(platformUserId)).thenReturn(Optional.of(contractor));

    AppSubscription cancelledSubscription = AppSubscription.builder()
        .id(UUID.randomUUID()).contractorId(contractorId)
        .appPlanVersionId(UUID.randomUUID())
        .status(SubscriptionStatus.CANCELLED)
        .currentPeriodStart(OffsetDateTime.now().minusDays(30))
        .currentPeriodEnd(OffsetDateTime.now().minusDays(1))
        .autoRenew(false)
        .createdAt(OffsetDateTime.now()).build();
    when(subscriptionRepo.findPlatformSubscriptionByContractorId(contractorId))
        .thenReturn(Optional.of(cancelledSubscription));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(platformUserId))
        .isInstanceOf(SubscriptionInvalidStateException.class);
    verify(subscriptionRepo, never()).save(any());
  }
}
