package io.cmartinezs.keygo.app.billing.platform.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.billing.platform.exception.ContractorNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.exception.SubscriptionNotFoundException;
import io.cmartinezs.keygo.app.billing.subscription.port.AppSubscriptionRepositoryPort;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
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
@DisplayName("GetPlatformSubscriptionUseCase")
class GetPlatformSubscriptionUseCaseTest {

  @Mock private ContractorRepositoryPort contractorRepo;
  @Mock private AppSubscriptionRepositoryPort subscriptionRepo;

  private GetPlatformSubscriptionUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetPlatformSubscriptionUseCase(contractorRepo, subscriptionRepo);
  }

  @Test
  @DisplayName("Debe retornar suscripción activa por platformUserId")
  void execute_returnsActiveSubscription() {
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
        .currentPeriodStart(OffsetDateTime.now().minusDays(15))
        .currentPeriodEnd(OffsetDateTime.now().plusDays(15))
        .autoRenew(true)
        .createdAt(OffsetDateTime.now()).build();
    when(subscriptionRepo.findPlatformSubscriptionByContractorId(contractorId))
        .thenReturn(Optional.of(subscription));

    // When
    AppSubscription result = useCase.execute(platformUserId);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(subscriptionId);
    assertThat(result.isActive()).isTrue();
    verify(contractorRepo).findByPlatformUserId(platformUserId);
    verify(subscriptionRepo).findPlatformSubscriptionByContractorId(contractorId);
  }

  @Test
  @DisplayName("Debe lanzar ContractorNotFoundException si no hay contractor")
  void execute_throwsWhenContractorNotFound() {
    // Given
    UUID platformUserId = UUID.randomUUID();
    when(contractorRepo.findByPlatformUserId(platformUserId)).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(platformUserId))
        .isInstanceOf(ContractorNotFoundException.class)
        .hasMessageContaining(platformUserId.toString());
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
        .isInstanceOf(SubscriptionNotFoundException.class)
        .hasMessageContaining(contractorId.toString());
  }
}
