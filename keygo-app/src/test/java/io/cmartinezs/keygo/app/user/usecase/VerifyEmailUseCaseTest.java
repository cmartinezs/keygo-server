package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.VerifyEmailCommand;
import io.cmartinezs.keygo.app.user.port.EmailVerificationRepositoryPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.exception.EmailVerificationExpiredException;
import io.cmartinezs.keygo.domain.user.exception.EmailVerificationInvalidException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.EmailVerification;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyEmailUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String CLIENT_ID = "client-123";
  private static final String EMAIL = "john@acme.com";
  private static final String VALID_CODE = "123456";

  @Mock TenantRepositoryPort tenantRepositoryPort;
  @Mock ClientAppRepositoryPort clientAppRepositoryPort;
  @Mock UserRepositoryPort userRepositoryPort;
  @Mock EmailVerificationRepositoryPort emailVerificationRepositoryPort;

  private VerifyEmailUseCase useCase;
  private Tenant activeTenant;
  private User pendingUser;

  @BeforeEach
  void setUp() {
    useCase = new VerifyEmailUseCase(
        tenantRepositoryPort, clientAppRepositoryPort,
        userRepositoryPort, emailVerificationRepositoryPort);

    activeTenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME Corp")
        .ownerEmail("owner@acme.com")
        .status(TenantStatus.ACTIVE)
        .build();

    pendingUser = User.builder()
        .id(UserId.generate())
        .tenantId(activeTenant.getId())
        .username(Username.of("johndoe"))
        .email(EmailAddress.of(EMAIL))
        .passwordHash(PasswordHash.of("$2a$10$hash"))
        .status(UserStatus.PENDING)
        .build();

    ClientApp clientApp = ClientApp.builder()
        .id(ClientAppId.generate())
        .clientId(ClientId.of(CLIENT_ID))
        .tenantId(activeTenant.getId())
        .name("My App")
        .type(ClientType.PUBLIC)
        .status(ClientAppStatus.ACTIVE)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .build();

    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(activeTenant));
    when(clientAppRepositoryPort.findByClientIdAndTenantId(ClientId.of(CLIENT_ID), activeTenant.getId()))
        .thenReturn(Optional.of(clientApp));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.of(pendingUser));
  }

  @Test
  void activatesUserWhenCodeIsValid() {
    // Given
    EmailVerification verification = EmailVerification.create(
        pendingUser.getId(), activeTenant.getId(), VALID_CODE,
        Instant.now().plus(30, ChronoUnit.MINUTES));
    when(emailVerificationRepositoryPort.findLatestByUserIdAndTenantId(any(), any()))
        .thenReturn(Optional.of(verification));
    when(emailVerificationRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    when(userRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    User result = useCase.execute(new VerifyEmailCommand(TENANT_SLUG, CLIENT_ID, EMAIL, VALID_CODE));

    // Then
    assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
    verify(emailVerificationRepositoryPort).save(any());
    verify(userRepositoryPort).save(any());
  }

  @Test
  void throwsExpiredExceptionWhenCodeIsExpired() {
    // Given
    EmailVerification expiredVerification = EmailVerification.create(
        pendingUser.getId(), activeTenant.getId(), VALID_CODE,
        Instant.now().minus(1, ChronoUnit.MINUTES));
    when(emailVerificationRepositoryPort.findLatestByUserIdAndTenantId(any(), any()))
        .thenReturn(Optional.of(expiredVerification));

    // When / Then
      VerifyEmailCommand command = new VerifyEmailCommand(TENANT_SLUG, CLIENT_ID, EMAIL, VALID_CODE);
      assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(EmailVerificationExpiredException.class);
  }

  @Test
  void throwsInvalidExceptionWhenCodeIsWrong() {
    // Given
    EmailVerification verification = EmailVerification.create(
        pendingUser.getId(), activeTenant.getId(), VALID_CODE,
        Instant.now().plus(30, ChronoUnit.MINUTES));
    when(emailVerificationRepositoryPort.findLatestByUserIdAndTenantId(any(), any()))
        .thenReturn(Optional.of(verification));

    // When / Then
      VerifyEmailCommand command = new VerifyEmailCommand(TENANT_SLUG, CLIENT_ID, EMAIL, "999999");
      assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(EmailVerificationInvalidException.class);
  }

  @Test
  void throwsInvalidExceptionWhenCodeIsAlreadyUsed() {
    // Given
    EmailVerification verification = EmailVerification.create(
        pendingUser.getId(), activeTenant.getId(), VALID_CODE,
        Instant.now().plus(30, ChronoUnit.MINUTES));
    verification.markUsed();
    when(emailVerificationRepositoryPort.findLatestByUserIdAndTenantId(any(), any()))
        .thenReturn(Optional.of(verification));

    // When / Then
      VerifyEmailCommand command = new VerifyEmailCommand(TENANT_SLUG, CLIENT_ID, EMAIL, VALID_CODE);
      assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(EmailVerificationInvalidException.class);
  }
}

