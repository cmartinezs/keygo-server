package io.cmartinezs.keygo.app.user.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import io.cmartinezs.keygo.domain.user.exception.PlatformUserSuspendedException;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.exception.UserPasswordResetRequiredException;
import io.cmartinezs.keygo.domain.user.exception.UserPendingVerificationException;
import io.cmartinezs.keygo.domain.user.exception.UserSuspendedException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ValidateUserCredentialsUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String EMAIL = "user@acme.local";
  private static final String USERNAME = "acme_user";
  private static final String RAW_PASSWORD = "SecurePass123!";
  private static final String HASHED_PASSWORD = "$2a$10$hashedValue";
  private static final UUID PLATFORM_USER_ID = UUID.randomUUID();

  @Mock private TenantRepositoryPort tenantRepositoryPort;
  @Mock private UserRepositoryPort userRepositoryPort;
  @Mock private CredentialEncoderPort credentialEncoderPort;
  @Mock private PlatformUserRepositoryPort platformUserRepositoryPort;

  @InjectMocks private ValidateUserCredentialsUseCase useCase;

  // ── Happy path ──────────────────────────────────────────────────────────────

  @Test
  void execute_happyPath_returnsAuthenticatedUser() {
    // Given
    Tenant tenant = buildTenant();
    User user = buildUser(UserStatus.ACTIVE, null);
    when(tenantRepositoryPort.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(user));
    when(credentialEncoderPort.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

    // When
    User result = useCase.execute(TENANT_SLUG, EMAIL, RAW_PASSWORD);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getEmail().value()).isEqualTo(EMAIL);
  }

  // ── Tenant not found ───────────────────────────────────────────────────────

  @Test
  void execute_tenantNotFound_throwsTenantNotFoundException() {
    // Given
    when(tenantRepositoryPort.findBySlug(any(TenantSlug.class))).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, EMAIL, RAW_PASSWORD))
        .isInstanceOf(TenantNotFoundException.class);
  }

  // ── User not found ─────────────────────────────────────────────────────────

  @Test
  void execute_userNotFound_throwsUserNotFoundException() {
    // Given
    Tenant tenant = buildTenant();
    when(tenantRepositoryPort.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, EMAIL, RAW_PASSWORD))
        .isInstanceOf(UserNotFoundException.class);
  }

  // ── Wrong password ─────────────────────────────────────────────────────────

  @Test
  void execute_wrongPassword_throwsInvalidCredentialsException() {
    // Given
    Tenant tenant = buildTenant();
    User user = buildUser(UserStatus.ACTIVE, null);
    when(tenantRepositoryPort.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(user));
    when(credentialEncoderPort.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(false);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, EMAIL, RAW_PASSWORD))
        .isInstanceOf(InvalidCredentialsException.class);
  }

  // ── Tenant-level status checks ─────────────────────────────────────────────

  @Test
  void execute_pendingUser_throwsUserPendingVerificationException() {
    // Given
    Tenant tenant = buildTenant();
    User user = buildUser(UserStatus.PENDING, null);
    when(tenantRepositoryPort.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(user));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, EMAIL, RAW_PASSWORD))
        .isInstanceOf(UserPendingVerificationException.class);
  }

  @Test
  void execute_suspendedUser_throwsUserSuspendedException() {
    // Given
    Tenant tenant = buildTenant();
    User user = buildUser(UserStatus.SUSPENDED, null);
    when(tenantRepositoryPort.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(user));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, EMAIL, RAW_PASSWORD))
        .isInstanceOf(UserSuspendedException.class);
  }

  @Test
  void execute_resetPasswordUser_throwsUserPasswordResetRequiredException() {
    // Given
    Tenant tenant = buildTenant();
    User user = buildUser(UserStatus.RESET_PASSWORD, null);
    when(tenantRepositoryPort.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(user));
    when(credentialEncoderPort.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, EMAIL, RAW_PASSWORD))
        .isInstanceOf(UserPasswordResetRequiredException.class)
        .hasMessageContaining(USERNAME);
  }

  // ── Platform user cascade ──────────────────────────────────────────────────

  @Test
  void execute_linkedPlatformUserSuspended_throwsPlatformUserSuspendedException() {
    // Given
    Tenant tenant = buildTenant();
    User user = buildUser(UserStatus.ACTIVE, PLATFORM_USER_ID);
    PlatformUser suspendedPlatformUser = buildPlatformUser(UserStatus.SUSPENDED);

    when(tenantRepositoryPort.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(user));
    when(platformUserRepositoryPort.findById(any(UserId.class)))
        .thenReturn(Optional.of(suspendedPlatformUser));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, EMAIL, RAW_PASSWORD))
        .isInstanceOf(PlatformUserSuspendedException.class)
        .hasMessageContaining("platform_admin");
  }

  @Test
  void execute_linkedPlatformUserActive_proceedsNormally() {
    // Given
    Tenant tenant = buildTenant();
    User user = buildUser(UserStatus.ACTIVE, PLATFORM_USER_ID);
    PlatformUser activePlatformUser = buildPlatformUser(UserStatus.ACTIVE);

    when(tenantRepositoryPort.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(user));
    when(platformUserRepositoryPort.findById(any(UserId.class)))
        .thenReturn(Optional.of(activePlatformUser));
    when(credentialEncoderPort.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

    // When
    User result = useCase.execute(TENANT_SLUG, EMAIL, RAW_PASSWORD);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.isActive()).isTrue();
  }

  @Test
  void execute_noPlatformUserLink_proceedsWithoutCascade() {
    // Given — user has no platformUserId
    Tenant tenant = buildTenant();
    User user = buildUser(UserStatus.ACTIVE, null);

    when(tenantRepositoryPort.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(user));
    when(credentialEncoderPort.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);

    // When
    User result = useCase.execute(TENANT_SLUG, EMAIL, RAW_PASSWORD);

    // Then
    assertThat(result).isNotNull();
  }

  // ── Helpers ─────────────────────────────────────────────────────────────────

  private Tenant buildTenant() {
    return Tenant.builder()
        .id(new TenantId(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Acme Corp")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private User buildUser(UserStatus status, UUID platformUserId) {
    return User.builder()
        .id(UserId.generate())
        .tenantId(new TenantId(UUID.randomUUID()))
        .username(Username.of(USERNAME))
        .email(EmailAddress.of(EMAIL))
        .passwordHash(PasswordHash.of(HASHED_PASSWORD))
        .status(status)
        .platformUserId(platformUserId)
        .build();
  }

  private PlatformUser buildPlatformUser(UserStatus status) {
    return PlatformUser.builder()
        .id(new UserId(PLATFORM_USER_ID))
        .username(Username.of("platform_admin"))
        .email(EmailAddress.of("admin@keygo.local"))
        .passwordHash(PasswordHash.of(HASHED_PASSWORD))
        .status(status)
        .build();
  }
}
