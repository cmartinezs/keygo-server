package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.GetNotificationPreferencesCommand;
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
 * Tests unitarios para GetNotificationPreferencesUseCase.
 */
@ExtendWith(MockitoExtension.class)
class GetNotificationPreferencesUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String BEARER_TOKEN = "valid.jwt.token";

  @Mock SigningKeyRepositoryPort signingKeyRepository;
  @Mock AccessTokenVerifierPort accessTokenVerifier;
  @Mock TenantRepositoryPort tenantRepository;
  @Mock NotificationPreferencesRepositoryPort preferencesRepository;

  private GetNotificationPreferencesUseCase useCase;
  private Tenant tenant;
  private UUID userId;
  private TenantId tenantId;

  @BeforeEach
  void setUp() {
    useCase = new GetNotificationPreferencesUseCase(
        signingKeyRepository, accessTokenVerifier, tenantRepository, preferencesRepository);

    userId = UUID.randomUUID();
    tenantId = TenantId.of(UUID.randomUUID());

    tenant = Tenant.builder()
        .id(tenantId)
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME").ownerEmail("owner@acme.com")
        .status(TenantStatus.ACTIVE).build();

    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(mock(SigningKey.class)));
    when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("sub", userId.toString()));
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
  }

  // ─── Escenario 1: con registro persistido ────────────────────────────────

  @Test
  void execute_shouldReturnStoredPreferences_whenRecordExists() {
    // Given
    var stored = NotificationPreferences.reconstitute(
        new UserId(userId), tenantId, true, false, true, true, false);

    when(preferencesRepository.findByUserIdAndTenantId(any(), any()))
        .thenReturn(Optional.of(stored));

    // When
    NotificationPreferencesResult result = useCase.execute(
        new GetNotificationPreferencesCommand(TENANT_SLUG, BEARER_TOKEN));

    // Then
    assertThat(result.securityAlertsEmail()).isTrue();
    assertThat(result.securityAlertsInApp()).isFalse();
    assertThat(result.billingAlertsEmail()).isTrue();
    assertThat(result.productUpdatesEmail()).isTrue();
    assertThat(result.weeklyDigest()).isFalse();
  }

  // ─── Escenario 2: sin registro → defaults ────────────────────────────────

  @Test
  void execute_shouldReturnDefaults_whenNoRecordExists() {
    // Given
    when(preferencesRepository.findByUserIdAndTenantId(any(), any()))
        .thenReturn(Optional.empty());

    // When
    NotificationPreferencesResult result = useCase.execute(
        new GetNotificationPreferencesCommand(TENANT_SLUG, BEARER_TOKEN));

    // Then — valores por defecto
    assertThat(result.securityAlertsEmail()).isTrue();
    assertThat(result.securityAlertsInApp()).isTrue();
    assertThat(result.billingAlertsEmail()).isTrue();
    assertThat(result.productUpdatesEmail()).isFalse();
    assertThat(result.weeklyDigest()).isFalse();
  }
}
