package io.cmartinezs.keygo.app.auth.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.auth.command.IssueAuthorizationCodeCommand;
import io.cmartinezs.keygo.app.auth.port.AuthorizationCodeRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.ClockPort;
import io.cmartinezs.keygo.app.auth.result.AuthorizationCodeIssuedResult;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.membership.exception.MembershipInactiveException;
import io.cmartinezs.keygo.domain.membership.exception.MembershipPendingException;
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
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssueAuthorizationCodeUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String CLIENT_ID = "acme-web";
  private static final String REDIRECT_URI = "https://acme.example.com/callback";
  private static final String SCOPES = "openid profile";
  private static final String CODE_CHALLENGE = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
  private static final String CODE_CHALLENGE_METHOD = "S256";

  private static final UUID TENANT_UUID = UUID.randomUUID();
  private static final UUID USER_UUID = UUID.randomUUID();
  private static final UUID CLIENT_APP_UUID = UUID.randomUUID();

  @Mock private AuthorizationCodeRepositoryPort authorizationCodeRepository;
  @Mock private ClientAppRepositoryPort clientAppRepository;
  @Mock private UserRepositoryPort userRepository;
  @Mock private MembershipRepositoryPort membershipRepository;
  @Mock private TenantRepositoryPort tenantRepository;
  @Mock private ClockPort clock;

  @InjectMocks private IssueAuthorizationCodeUseCase useCase;

  // ── Happy path ──────────────────────────────────────────────────────────────

  @Test
  void execute_happyPath_returnsAuthorizationCode() {
    // Given
    IssueAuthorizationCodeCommand command = buildCommand();
    setupHappyPath(MembershipStatus.ACTIVE);

    // When
    AuthorizationCodeIssuedResult result = useCase.execute(command);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.code()).isNotBlank();
    assertThat(result.redirectUri()).isEqualTo(REDIRECT_URI);
  }

  // ── Membership suspended ───────────────────────────────────────────────────

  @Test
  void execute_membershipSuspended_throwsMembershipInactiveException() {
    // Given
    IssueAuthorizationCodeCommand command = buildCommand();
    setupHappyPath(MembershipStatus.SUSPENDED);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(MembershipInactiveException.class)
        .hasMessageContaining("suspended");
  }

  // ── Membership pending ─────────────────────────────────────────────────────

  @Test
  void execute_membershipPending_throwsMembershipPendingException() {
    // Given
    IssueAuthorizationCodeCommand command = buildCommand();
    setupHappyPath(MembershipStatus.PENDING);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(MembershipPendingException.class)
        .hasMessageContaining("pending");
  }

  // ── Membership not found ───────────────────────────────────────────────────

  @Test
  void execute_membershipNotFound_throwsMembershipInactiveException() {
    // Given
    IssueAuthorizationCodeCommand command = buildCommand();
    Tenant tenant = buildTenant();
    User user = buildUser();
    ClientApp clientApp = buildClientApp();

    when(tenantRepository.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(user));
    when(clientAppRepository.findByClientIdAndTenantId(any(), any())).thenReturn(Optional.of(clientApp));
    when(membershipRepository.findByUserAndClientApp(any(), any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(MembershipInactiveException.class)
        .hasMessageContaining("no access");
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────

  private void setupHappyPath(MembershipStatus membershipStatus) {
    Tenant tenant = buildTenant();
    User user = buildUser();
    ClientApp clientApp = buildClientApp();
    Membership membership = buildMembership(membershipStatus);

    when(tenantRepository.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(userRepository.findByIdAndTenantId(any(), any())).thenReturn(Optional.of(user));
    when(clientAppRepository.findByClientIdAndTenantId(any(), any())).thenReturn(Optional.of(clientApp));
    when(membershipRepository.findByUserAndClientApp(any(), any())).thenReturn(Optional.of(membership));
    if (MembershipStatus.ACTIVE.equals(membershipStatus)) {
      when(clock.futureSeconds(600L)).thenReturn(Instant.now().plusSeconds(600));
    }
  }

  private IssueAuthorizationCodeCommand buildCommand() {
    return new IssueAuthorizationCodeCommand(
        TENANT_SLUG, CLIENT_ID, USER_UUID.toString(), REDIRECT_URI,
        SCOPES, CODE_CHALLENGE, CODE_CHALLENGE_METHOD);
  }

  private Tenant buildTenant() {
    return Tenant.builder()
        .id(new TenantId(TENANT_UUID))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Acme Corp")
        .ownerEmail("owner@acme.local")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private User buildUser() {
    return User.builder()
        .id(new UserId(USER_UUID))
        .tenantId(new TenantId(TENANT_UUID))
        .username(Username.of("acme_user"))
        .email(EmailAddress.of("user@acme.local"))
        .passwordHash(PasswordHash.of("$2a$10$hash"))
        .status(UserStatus.ACTIVE)
        .build();
  }

  private ClientApp buildClientApp() {
    return ClientApp.builder()
        .id(new ClientAppId(CLIENT_APP_UUID))
        .clientId(new ClientId(CLIENT_ID))
        .tenantId(new TenantId(TENANT_UUID))
        .name("Acme Web App")
        .type(ClientType.PUBLIC)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Collections.emptySet()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  private Membership buildMembership(MembershipStatus status) {
    return Membership.builder()
        .id(new MembershipId(UUID.randomUUID()))
        .userId(new UserId(USER_UUID))
        .clientAppId(new ClientAppId(CLIENT_APP_UUID))
        .status(status)
        .build();
  }
}
