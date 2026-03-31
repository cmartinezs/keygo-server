package io.cmartinezs.keygo.app.user.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.auth.port.ClockPort;
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
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResendVerificationEmailUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String CLIENT_ID = "client-123";
  private static final String EMAIL = "john@acme.com";
  /** Fixed clock reference instant used in all tests. */
  private static final Instant NOW = Instant.parse("2026-03-31T10:00:00Z");

  @Mock TenantRepositoryPort tenantRepositoryPort;
  @Mock ClientAppRepositoryPort clientAppRepositoryPort;
  @Mock UserRepositoryPort userRepositoryPort;
  @Mock EmailVerificationRepositoryPort emailVerificationRepositoryPort;
  @Mock EmailNotificationPort emailNotificationPort;
  @Mock ClockPort clock;

  private ResendVerificationEmailUseCase useCase;
  private Tenant activeTenant;
  private User pendingUser;

  @BeforeEach
  void setUp() {
    // Fixed clock so time-based assertions are deterministic.
    // lenient() avoids UnnecessaryStubbing in tests that throw before clock.now() is called.
    lenient().when(clock.now()).thenReturn(NOW);

    useCase =
        new ResendVerificationEmailUseCase(
            tenantRepositoryPort,
            clientAppRepositoryPort,
            userRepositoryPort,
            emailVerificationRepositoryPort,
            emailNotificationPort,
            clock);

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
  void generatesNewCodeAndSendsEmailWhenPreviousCodeHasExpired() {
    // Given — saveIfExpiredOrAbsent returns the NEW verification (expired code was replaced)
    EmailVerification newVerification =
        EmailVerification.create(
            pendingUser.getId(),
            activeTenant.getId(),
            "999999",
            NOW.plus(30, ChronoUnit.MINUTES));
    when(emailVerificationRepositoryPort.saveIfExpiredOrAbsent(any(), any(), any()))
        .thenReturn(newVerification);

    // When
    useCase.execute(new ResendVerificationCommand(TENANT_SLUG, CLIENT_ID, EMAIL));

    // Then — saveIfExpiredOrAbsent was called and the new code was sent
    verify(emailVerificationRepositoryPort).saveIfExpiredOrAbsent(any(), any(), any());
    verify(emailNotificationPort).sendVerificationEmail(eq(EMAIL), anyString(), eq("999999"));
  }

  @Test
  void generatesNewCodeWhenNoPreviousCodeExists() {
    // Given — saveIfExpiredOrAbsent returns a freshly created verification
    EmailVerification newVerification =
        EmailVerification.create(
            pendingUser.getId(),
            activeTenant.getId(),
            "111111",
            NOW.plus(30, ChronoUnit.MINUTES));
    when(emailVerificationRepositoryPort.saveIfExpiredOrAbsent(any(), any(), any()))
        .thenReturn(newVerification);

    // When
    useCase.execute(new ResendVerificationCommand(TENANT_SLUG, CLIENT_ID, EMAIL));

    // Then
    verify(emailVerificationRepositoryPort).saveIfExpiredOrAbsent(any(), any(), any());
    verify(emailNotificationPort).sendVerificationEmail(eq(EMAIL), anyString(), eq("111111"));
  }

  @Test
  void resendsExistingCodeWhenStillActive() {
    // Given — saveIfExpiredOrAbsent returns the EXISTING (still-valid) verification
    EmailVerification existingActive =
        EmailVerification.create(
            pendingUser.getId(),
            activeTenant.getId(),
            "222222",   // the original code sent to the user
            NOW.plus(29, ChronoUnit.MINUTES)); // 29 min remaining
    when(emailVerificationRepositoryPort.saveIfExpiredOrAbsent(any(), any(), any()))
        .thenReturn(existingActive);

    // When
    useCase.execute(new ResendVerificationCommand(TENANT_SLUG, CLIENT_ID, EMAIL));

    // Then — no additional persist; same code is re-sent to the user
    verify(emailVerificationRepositoryPort).saveIfExpiredOrAbsent(any(), any(), any());
    verify(emailNotificationPort).sendVerificationEmail(eq(EMAIL), anyString(), eq("222222"));
  }

  @Test
  void verifiesNewVerificationPassedToAdapterHas30MinuteExpiry() {
    // Given
    EmailVerification dummy =
        EmailVerification.create(
            pendingUser.getId(), activeTenant.getId(), "000000",
            NOW.plus(30, ChronoUnit.MINUTES));
    when(emailVerificationRepositoryPort.saveIfExpiredOrAbsent(any(), any(), any()))
        .thenReturn(dummy);

    // When
    useCase.execute(new ResendVerificationCommand(TENANT_SLUG, CLIENT_ID, EMAIL));

    // Then — capture the newVerification passed to the port and verify its expiry window
    ArgumentCaptor<EmailVerification> captor = ArgumentCaptor.forClass(EmailVerification.class);
    verify(emailVerificationRepositoryPort).saveIfExpiredOrAbsent(any(), any(), captor.capture());
    EmailVerification captured = captor.getValue();
    assertThat(captured.getExpiresAt()).isEqualTo(NOW.plus(30, ChronoUnit.MINUTES));
    assertThat(captured.getCode()).hasSize(6);
  }

  @Test
  void throwsUserNotFoundWhenUserDoesNotExist() {
    // Given
    when(userRepositoryPort.findByTenantIdAndEmail(any(), any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(
            () -> useCase.execute(new ResendVerificationCommand(TENANT_SLUG, CLIENT_ID, EMAIL)))
        .isInstanceOf(UserNotFoundException.class);
    verify(emailNotificationPort, never()).sendVerificationEmail(any(), any(), any());
  }
}




