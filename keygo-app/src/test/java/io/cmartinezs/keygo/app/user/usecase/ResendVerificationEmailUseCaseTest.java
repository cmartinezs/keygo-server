package io.cmartinezs.keygo.app.user.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.ResendVerificationCommand;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
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
import io.cmartinezs.keygo.domain.user.exception.EmailVerificationStillActiveException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.EmailVerification;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResendVerificationEmailUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String CLIENT_ID = "client-123";
  private static final String EMAIL = "john@acme.com";

  @Mock TenantRepositoryPort tenantRepositoryPort;
  @Mock ClientAppRepositoryPort clientAppRepositoryPort;
  @Mock UserRepositoryPort userRepositoryPort;
  @Mock EmailVerificationRepositoryPort emailVerificationRepositoryPort;
  @Mock EmailNotificationPort emailNotificationPort;

  private ResendVerificationEmailUseCase useCase;
  private Tenant activeTenant;
  private User pendingUser;

  @BeforeEach
  void setUp() {
    useCase =
        new ResendVerificationEmailUseCase(
            tenantRepositoryPort,
            clientAppRepositoryPort,
            userRepositoryPort,
            emailVerificationRepositoryPort,
            emailNotificationPort);

    activeTenant =
        Tenant.builder()
            .id(TenantId.of(UUID.randomUUID()))
            .slug(TenantSlug.of(TENANT_SLUG))
            .name("ACME Corp")
            .ownerEmail("owner@acme.com")
            .status(TenantStatus.ACTIVE)
            .build();

    pendingUser =
        User.builder()
            .id(UserId.generate())
            .tenantId(activeTenant.getId())
            .username(Username.of("johndoe"))
            .email(EmailAddress.of(EMAIL))
            .passwordHash(PasswordHash.of("$2a$10$hash"))
            .status(UserStatus.PENDING)
            .build();

    ClientApp clientApp =
        ClientApp.builder()
            .id(ClientAppId.generate())
            .clientId(ClientId.of(CLIENT_ID))
            .tenantId(activeTenant.getId())
            .name("My App")
            .type(ClientType.PUBLIC)
            .status(ClientAppStatus.ACTIVE)
            .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
            .build();

    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(activeTenant));
    when(clientAppRepositoryPort.findByClientIdAndTenantId(
            ClientId.of(CLIENT_ID), activeTenant.getId()))
        .thenReturn(Optional.of(clientApp));
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any()))
        .thenReturn(Optional.of(pendingUser));
  }

  @Test
  void resendsCodeWhenPreviousCodeHasExpired() {
    // Given
    EmailVerification expired =
        EmailVerification.create(
            pendingUser.getId(),
            activeTenant.getId(),
            "111111",
            Instant.now().minus(1, ChronoUnit.MINUTES)); // expired
    when(emailVerificationRepositoryPort.findLatestByUserIdAndTenantId(any(), any()))
        .thenReturn(Optional.of(expired));
    when(emailVerificationRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(new ResendVerificationCommand(TENANT_SLUG, CLIENT_ID, EMAIL));

    // Then
    verify(emailVerificationRepositoryPort).save(any());
    verify(emailNotificationPort).sendVerificationEmail(anyString(), anyString(), anyString());
  }

  @Test
  void resendsCodeWhenNoPreviousCodeExists() {
    // Given — no previous verification
    when(emailVerificationRepositoryPort.findLatestByUserIdAndTenantId(any(), any()))
        .thenReturn(Optional.empty());
    when(emailVerificationRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    useCase.execute(new ResendVerificationCommand(TENANT_SLUG, CLIENT_ID, EMAIL));

    // Then
    verify(emailNotificationPort).sendVerificationEmail(anyString(), anyString(), anyString());
  }

  @Test
  void throwsWhenPreviousCodeIsStillActive() {
    // Given
    EmailVerification active =
        EmailVerification.create(
            pendingUser.getId(),
            activeTenant.getId(),
            "222222",
            Instant.now().plus(29, ChronoUnit.MINUTES)); // still active
    when(emailVerificationRepositoryPort.findLatestByUserIdAndTenantId(any(), any()))
        .thenReturn(Optional.of(active));

    // When / Then
    ResendVerificationCommand command =
        new ResendVerificationCommand(TENANT_SLUG, CLIENT_ID, EMAIL);
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(EmailVerificationStillActiveException.class);
    verify(emailNotificationPort, never()).sendVerificationEmail(any(), any(), any());
  }
}
