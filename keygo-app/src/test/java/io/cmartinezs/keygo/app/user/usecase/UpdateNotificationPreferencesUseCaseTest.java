package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.UpdateNotificationPreferencesCommand;
import io.cmartinezs.keygo.app.user.port.NotificationPreferencesRepositoryPort;
import io.cmartinezs.keygo.app.user.result.NotificationPreferencesResult;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.NotificationPreferences;
import io.cmartinezs.keygo.domain.user.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios para UpdateNotificationPreferencesUseCase.
 */
@ExtendWith(MockitoExtension.class)
class UpdateNotificationPreferencesUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String BEARER_TOKEN = "valid.jwt.token";

  @Mock SigningKeyRepositoryPort signingKeyRepository;
  @Mock AccessTokenVerifierPort accessTokenVerifier;
  @Mock TenantRepositoryPort tenantRepository;
  @Mock NotificationPreferencesRepositoryPort preferencesRepository;

  private UpdateNotificationPreferencesUseCase useCase;
  private Tenant tenant;
  private UUID userId;
  private TenantId tenantId;

  @BeforeEach
  void setUp() {
    useCase = new UpdateNotificationPreferencesUseCase(
        signingKeyRepository, accessTokenVerifier, tenantRepository, preferencesRepository);

    userId = UUID.randomUUID();
    tenantId = TenantId.of(UUID.randomUUID());

    tenant = Tenant.builder()
        .id(tenantId)
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME")
        .status(TenantStatus.ACTIVE).build();

    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(mock(SigningKey.class)));
    when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("sub", userId.toString()));
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
  }

  // ─── Escenario 1: upsert primera vez (sin registro previo) ───────────────

  @Test
  void execute_shouldCreatePreferences_whenNoRecordExists() {
    // Given
    when(preferencesRepository.findByUserIdAndTenantId(any(), any()))
        .thenReturn(Optional.empty());
    when(preferencesRepository.saveOrUpdate(any()))
        .thenAnswer(inv -> inv.getArgument(0));

    // When
    NotificationPreferencesResult result = useCase.execute(
        new UpdateNotificationPreferencesCommand(TENANT_SLUG, BEARER_TOKEN,
            false, false, true, true, true));

    // Then
    assertThat(result.securityAlertsEmail()).isFalse();
    assertThat(result.securityAlertsInApp()).isFalse();
    assertThat(result.billingAlertsEmail()).isTrue();
    assertThat(result.productUpdatesEmail()).isTrue();
    assertThat(result.weeklyDigest()).isTrue();
  }

  // ─── Escenario 2: actualizar registro existente ───────────────────────────

  @Test
  void execute_shouldUpdateExistingPreferences_whenRecordExists() {
    // Given
    var existing = NotificationPreferences.reconstitute(
        new UserId(userId), tenantId, true, true, true, false, false);

    when(preferencesRepository.findByUserIdAndTenantId(any(), any()))
        .thenReturn(Optional.of(existing));
    when(preferencesRepository.saveOrUpdate(any()))
        .thenAnswer(inv -> inv.getArgument(0));

    // When
    NotificationPreferencesResult result = useCase.execute(
        new UpdateNotificationPreferencesCommand(TENANT_SLUG, BEARER_TOKEN,
            true, false, true, true, false));

    // Then
    assertThat(result.securityAlertsEmail()).isTrue();
    assertThat(result.securityAlertsInApp()).isFalse();
    assertThat(result.billingAlertsEmail()).isTrue();
    assertThat(result.productUpdatesEmail()).isTrue();
    assertThat(result.weeklyDigest()).isFalse();
  }
}
