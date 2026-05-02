package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.GetUserAccessCommand;
import io.cmartinezs.keygo.app.user.result.UserAccessResult;
import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
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
 * Tests unitarios para GetUserAccessUseCase.
 */
@ExtendWith(MockitoExtension.class)
class GetUserAccessUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String BEARER_TOKEN = "valid.jwt.token";

  @Mock SigningKeyRepositoryPort signingKeyRepository;
  @Mock AccessTokenVerifierPort accessTokenVerifier;
  @Mock TenantRepositoryPort tenantRepository;
  @Mock MembershipRepositoryPort membershipRepository;
  @Mock ClientAppRepositoryPort clientAppRepository;

  private GetUserAccessUseCase useCase;
  private Tenant tenant;
  private UUID userId;

  @BeforeEach
  void setUp() {
    useCase = new GetUserAccessUseCase(
        signingKeyRepository, accessTokenVerifier,
        tenantRepository, membershipRepository, clientAppRepository);

    userId = UUID.randomUUID();

    tenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME")
        .status(TenantStatus.ACTIVE).build();

    when(signingKeyRepository.findPublishableKeys()).thenReturn(List.of(mock(SigningKey.class)));
    when(accessTokenVerifier.verify(eq(BEARER_TOKEN), any()))
        .thenReturn(Map.of("sub", userId.toString()));
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.of(tenant));
  }

  // ─── Escenario 1: usuario con membresías ─────────────────────────────────

  @Test
  void execute_shouldReturnAccessEntries_whenUserHasMemberships() {
    // Given
    UUID clientAppUuid = UUID.randomUUID();
    UUID membershipUuid = UUID.randomUUID();

    Membership membership = Membership.builder()
        .id(MembershipId.of(membershipUuid))
        .userId(new UserId(userId))
        .clientAppId(ClientAppId.of(clientAppUuid))
        .status(MembershipStatus.ACTIVE)
        .build();

    ClientApp clientApp = mock(ClientApp.class);
    when(clientApp.getName()).thenReturn("KeyGo UI");

    when(membershipRepository.findByUserIdAndTenantSlug(eq(userId), eq(TENANT_SLUG)))
        .thenReturn(List.of(membership));
    when(clientAppRepository.findById(any())).thenReturn(Optional.of(clientApp));
    when(membershipRepository.findRoleCodesByUserAndClientApp(eq(userId), eq(clientAppUuid)))
        .thenReturn(List.of("ADMIN_TENANT"));

    // When
    UserAccessResult result = useCase.execute(
        new GetUserAccessCommand(TENANT_SLUG, BEARER_TOKEN));

    // Then
    assertThat(result.entries()).hasSize(1);
    assertThat(result.entries().get(0).clientAppName()).isEqualTo("KeyGo UI");
    assertThat(result.entries().get(0).clientAppId()).isEqualTo(clientAppUuid);
    assertThat(result.entries().get(0).membershipId()).isEqualTo(membershipUuid);
    assertThat(result.entries().get(0).status()).isEqualTo("ACTIVE");
    assertThat(result.entries().get(0).roles()).containsExactly("ADMIN_TENANT");
  }

  // ─── Escenario 2: usuario sin membresías → lista vacía ───────────────────

  @Test
  void execute_shouldReturnEmptyList_whenUserHasNoMemberships() {
    // Given
    when(membershipRepository.findByUserIdAndTenantSlug(any(), any()))
        .thenReturn(List.of());

    // When
    UserAccessResult result = useCase.execute(
        new GetUserAccessCommand(TENANT_SLUG, BEARER_TOKEN));

    // Then
    assertThat(result.entries()).isEmpty();
  }

  // ─── Escenario 3: app no encontrada → nombre "Unknown" ───────────────────

  @Test
  void execute_shouldUseUnknownAppName_whenClientAppNotFound() {
    // Given
    UUID clientAppUuid = UUID.randomUUID();

    Membership membership = Membership.builder()
        .id(MembershipId.generate())
        .userId(new UserId(userId))
        .clientAppId(ClientAppId.of(clientAppUuid))
        .status(MembershipStatus.ACTIVE)
        .build();

    when(membershipRepository.findByUserIdAndTenantSlug(any(), any()))
        .thenReturn(List.of(membership));
    when(clientAppRepository.findById(any())).thenReturn(Optional.empty());
    when(membershipRepository.findRoleCodesByUserAndClientApp(any(), any()))
        .thenReturn(List.of());

    // When
    UserAccessResult result = useCase.execute(
        new GetUserAccessCommand(TENANT_SLUG, BEARER_TOKEN));

    // Then
    assertThat(result.entries().get(0).clientAppName()).isEqualTo("Unknown");
  }
}
