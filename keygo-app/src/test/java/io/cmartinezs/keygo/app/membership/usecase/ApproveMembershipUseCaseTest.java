package io.cmartinezs.keygo.app.membership.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedScope;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.membership.exception.MembershipAlreadyActiveException;
import io.cmartinezs.keygo.domain.membership.exception.MembershipAlreadySuspendedException;
import io.cmartinezs.keygo.domain.membership.exception.MembershipNotFoundException;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApproveMembershipUseCaseTest {

  private static final String TENANT_SLUG = "acme";

  @Mock
  private MembershipRepositoryPort membershipRepositoryPort;

  @Mock
  private TenantRepositoryPort tenantRepositoryPort;

  @Mock
  private UserRepositoryPort userRepositoryPort;

  @Mock
  private ClientAppRepositoryPort clientAppRepositoryPort;

  @Mock
  private EmailNotificationPort emailNotificationPort;

  @InjectMocks
  private ApproveMembershipUseCase useCase;

  @Test
  void execute_whenPendingMembership_shouldApproveAndReturnActive() {
    // Given
    MembershipId membershipId = MembershipId.generate();
    Membership pending = membership(membershipId, MembershipStatus.PENDING);
    when(membershipRepositoryPort.findByIdAndTenantSlug(membershipId, TENANT_SLUG))
        .thenReturn(Optional.of(pending));
    when(membershipRepositoryPort.update(any(Membership.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // When
    Membership result = useCase.execute(membershipId, TENANT_SLUG);

    // Then
    assertThat(result.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    verify(membershipRepositoryPort).update(pending);
  }

  @Test
  void execute_whenPendingMembership_shouldSendApprovalEmail() {
    // Given
    var membershipId = MembershipId.generate();
    var userId = UserId.of(UUID.randomUUID());
    var clientAppId = ClientAppId.of(UUID.randomUUID());
    var tenantId = TenantId.of(UUID.randomUUID());
    Membership pending = Membership.builder()
        .id(membershipId).userId(userId).clientAppId(clientAppId)
        .status(MembershipStatus.PENDING).build();

    when(membershipRepositoryPort.findByIdAndTenantSlug(membershipId, TENANT_SLUG))
        .thenReturn(Optional.of(pending));
    when(membershipRepositoryPort.update(any(Membership.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(tenant(tenantId)));
    when(userRepositoryPort.findByIdAndTenantId(userId, tenantId))
        .thenReturn(Optional.of(user(userId, tenantId)));
    when(clientAppRepositoryPort.findById(clientAppId))
        .thenReturn(Optional.of(clientApp(clientAppId)));

    // When
    useCase.execute(membershipId, TENANT_SLUG);

    // Then
    verify(emailNotificationPort).sendMembershipApprovedEmail(
        "user@acme.local", "acme_user", "Test App");
  }

  @Test
  void execute_whenEmailFails_shouldStillReturnApprovedMembership() {
    // Given
    var membershipId = MembershipId.generate();
    var userId = UserId.of(UUID.randomUUID());
    var clientAppId = ClientAppId.of(UUID.randomUUID());
    var tenantId = TenantId.of(UUID.randomUUID());
    Membership pending = Membership.builder()
        .id(membershipId).userId(userId).clientAppId(clientAppId)
        .status(MembershipStatus.PENDING).build();

    when(membershipRepositoryPort.findByIdAndTenantSlug(membershipId, TENANT_SLUG))
        .thenReturn(Optional.of(pending));
    when(membershipRepositoryPort.update(any(Membership.class)))
        .thenAnswer(inv -> inv.getArgument(0));
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(tenant(tenantId)));
    when(userRepositoryPort.findByIdAndTenantId(userId, tenantId))
        .thenReturn(Optional.of(user(userId, tenantId)));
    when(clientAppRepositoryPort.findById(clientAppId))
        .thenReturn(Optional.of(clientApp(clientAppId)));
    doThrow(new RuntimeException("SMTP down"))
        .when(emailNotificationPort).sendMembershipApprovedEmail(anyString(), anyString(), anyString());

    // When
    Membership result = useCase.execute(membershipId, TENANT_SLUG);

    // Then — approval persists even if email fails
    assertThat(result.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    verify(membershipRepositoryPort).update(pending);
  }

  @Test
  void execute_whenMembershipNotFound_shouldThrowMembershipNotFoundException() {
    // Given
    MembershipId membershipId = MembershipId.generate();
    when(membershipRepositoryPort.findByIdAndTenantSlug(membershipId, TENANT_SLUG))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(membershipId, TENANT_SLUG))
        .isInstanceOf(MembershipNotFoundException.class);

    verify(membershipRepositoryPort, never()).update(any());
  }

  @Test
  void execute_whenMembershipAlreadyActive_shouldThrowMembershipAlreadyActiveException() {
    // Given
    MembershipId membershipId = MembershipId.generate();
    Membership active = membership(membershipId, MembershipStatus.ACTIVE);
    when(membershipRepositoryPort.findByIdAndTenantSlug(membershipId, TENANT_SLUG))
        .thenReturn(Optional.of(active));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(membershipId, TENANT_SLUG))
        .isInstanceOf(MembershipAlreadyActiveException.class);

    verify(membershipRepositoryPort, never()).update(any());
  }

  @Test
  void execute_whenMembershipSuspended_shouldThrowMembershipAlreadySuspendedException() {
    // Given
    MembershipId membershipId = MembershipId.generate();
    Membership suspended = membership(membershipId, MembershipStatus.SUSPENDED);
    when(membershipRepositoryPort.findByIdAndTenantSlug(membershipId, TENANT_SLUG))
        .thenReturn(Optional.of(suspended));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(membershipId, TENANT_SLUG))
        .isInstanceOf(MembershipAlreadySuspendedException.class);

    verify(membershipRepositoryPort, never()).update(any());
  }

  @Test
  void execute_shouldNeverCallUnscopedFindById() {
    // Given
    MembershipId membershipId = MembershipId.generate();
    Membership pending = membership(membershipId, MembershipStatus.PENDING);
    when(membershipRepositoryPort.findByIdAndTenantSlug(membershipId, TENANT_SLUG))
        .thenReturn(Optional.of(pending));
    when(membershipRepositoryPort.update(any(Membership.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(membershipId, TENANT_SLUG);

    // Then — the unscoped findById must never be called
    verify(membershipRepositoryPort, never()).findById(membershipId);
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────

  private Membership membership(MembershipId membershipId, MembershipStatus status) {
    return Membership.builder()
        .id(membershipId)
        .userId(UserId.of(UUID.randomUUID()))
        .clientAppId(ClientAppId.of(UUID.randomUUID()))
        .status(status)
        .build();
  }

  private Tenant tenant(TenantId tenantId) {
    return Tenant.builder()
        .id(tenantId)
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Acme Corp")
        .ownerEmail("admin@acme.local")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private User user(UserId userId, TenantId tenantId) {
    return User.builder()
        .id(userId)
        .tenantId(tenantId)
        .username(Username.of("acme_user"))
        .email(EmailAddress.of("user@acme.local"))
        .passwordHash(PasswordHash.of("hashed"))
        .status(UserStatus.ACTIVE)
        .build();
  }

  private ClientApp clientApp(ClientAppId clientAppId) {
    return ClientApp.builder()
        .id(clientAppId)
        .tenantId(TenantId.of(UUID.randomUUID()))
        .name("Test App")
        .clientId(ClientId.of("test-app-client-id"))
        .hashedSecret("secret-hash")
        .type(ClientType.CONFIDENTIAL)
        .status(ClientAppStatus.ACTIVE)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of(new AllowedScope("openid"))))
        .build();
  }
}
