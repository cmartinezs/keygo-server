package io.cmartinezs.keygo.app.user.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.app.user.command.RegisterTenantUserCommand;
import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import io.cmartinezs.keygo.app.user.port.EmailVerificationRepositoryPort;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.user.port.UserRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.domain.user.exception.DuplicateUserException;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterTenantUserUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String CLIENT_ID = "client-123";
  private static final String USERNAME = "johndoe";
  private static final String EMAIL = "john@acme.com";
  private static final String RAW_PASSWORD = "SecurePass123!";  // 14 chars: uppercase, lowercase, digit, special
  private static final String HASHED_PASSWORD = "$2a$10$hash";

  @Mock TenantRepositoryPort tenantRepositoryPort;
  @Mock ClientAppRepositoryPort clientAppRepositoryPort;
  @Mock UserRepositoryPort userRepositoryPort;
  @Mock CredentialEncoderPort credentialEncoderPort;
  @Mock EmailVerificationRepositoryPort emailVerificationRepositoryPort;
  @Mock EmailNotificationPort emailNotificationPort;

  private RegisterTenantUserUseCase useCase;
  private Tenant activeTenant;
  private ClientApp clientApp;

  @BeforeEach
  void setUp() {
    useCase = new RegisterTenantUserUseCase(
        tenantRepositoryPort, clientAppRepositoryPort, userRepositoryPort,
        credentialEncoderPort, emailVerificationRepositoryPort, emailNotificationPort);

    activeTenant = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME Corp")
        .ownerEmail("owner@acme.com")
        .status(TenantStatus.ACTIVE)
        .build();

    clientApp = ClientApp.builder()
        .id(ClientAppId.generate())
        .clientId(ClientId.of(CLIENT_ID))
        .tenantId(activeTenant.getId())
        .name("My App")
        .type(ClientType.PUBLIC)
        .status(ClientAppStatus.ACTIVE)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .build();
  }

  @Test
  void registersUserWithPendingStatusAndSendsEmail() {
    // Given
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(activeTenant));
    when(clientAppRepositoryPort.findByClientIdAndTenantId(ClientId.of(CLIENT_ID), activeTenant.getId()))
        .thenReturn(Optional.of(clientApp));
    when(userRepositoryPort.existsByTenantIdAndEmail(any(), any())).thenReturn(false);
    when(userRepositoryPort.existsByTenantIdAndUsername(any(), any())).thenReturn(false);
    when(credentialEncoderPort.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    when(userRepositoryPort.save(userCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
    when(emailVerificationRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    RegisterTenantUserCommand command = new RegisterTenantUserCommand(
        TENANT_SLUG, CLIENT_ID, USERNAME, EMAIL, RAW_PASSWORD, "John", "Doe");

    // When
    User result = useCase.execute(command);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(UserStatus.PENDING);
    assertThat(result.getEmail().value()).isEqualTo(EMAIL);
    assertThat(result.getUsername().value()).isEqualTo(USERNAME);

    verify(emailVerificationRepositoryPort).save(any());
    verify(emailNotificationPort).sendVerificationEmail(anyString(), anyString(), anyString());
  }

  @Test
  void throwsTenantNotFoundWhenTenantDoesNotExist() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());
    RegisterTenantUserCommand command =
        new RegisterTenantUserCommand(TENANT_SLUG, CLIENT_ID, USERNAME, EMAIL, RAW_PASSWORD, null, null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(TenantNotFoundException.class);
    verify(emailNotificationPort, never()).sendVerificationEmail(any(), any(), any());
  }

  @Test
  void throwsTenantSuspendedWhenTenantIsSuspended() {
    // Given
    Tenant suspended = Tenant.builder()
        .id(TenantId.of(UUID.randomUUID()))
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("ACME Corp")
        .ownerEmail("owner@acme.com")
        .status(TenantStatus.SUSPENDED)
        .build();
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(suspended));
    RegisterTenantUserCommand command =
        new RegisterTenantUserCommand(TENANT_SLUG, CLIENT_ID, USERNAME, EMAIL, RAW_PASSWORD, null, null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(TenantSuspendedException.class);
    verify(emailNotificationPort, never()).sendVerificationEmail(any(), any(), any());
  }

  @Test
  void throwsClientAppNotFoundWhenAppDoesNotBelongToTenant() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(clientAppRepositoryPort.findByClientIdAndTenantId(any(), any())).thenReturn(Optional.empty());
    RegisterTenantUserCommand command =
        new RegisterTenantUserCommand(TENANT_SLUG, CLIENT_ID, USERNAME, EMAIL, RAW_PASSWORD, null, null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ClientAppNotFoundException.class);
    verify(emailNotificationPort, never()).sendVerificationEmail(any(), any(), any());
  }

  @Test
  void throwsDuplicateUserWhenEmailAlreadyExists() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(clientAppRepositoryPort.findByClientIdAndTenantId(any(), any())).thenReturn(Optional.of(clientApp));
    when(userRepositoryPort.existsByTenantIdAndEmail(any(), any())).thenReturn(true);
    RegisterTenantUserCommand command =
        new RegisterTenantUserCommand(TENANT_SLUG, CLIENT_ID, USERNAME, EMAIL, RAW_PASSWORD, null, null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(DuplicateUserException.class);
    verify(emailNotificationPort, never()).sendVerificationEmail(any(), any(), any());
  }

  @Test
  void throwsDuplicateUserWhenUsernameAlreadyExists() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(activeTenant));
    when(clientAppRepositoryPort.findByClientIdAndTenantId(any(), any())).thenReturn(Optional.of(clientApp));
    when(userRepositoryPort.existsByTenantIdAndEmail(any(), any())).thenReturn(false);
    when(userRepositoryPort.existsByTenantIdAndUsername(any(), any())).thenReturn(true);
    RegisterTenantUserCommand command =
        new RegisterTenantUserCommand(TENANT_SLUG, CLIENT_ID, USERNAME, EMAIL, RAW_PASSWORD, null, null);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(DuplicateUserException.class);
    verify(emailNotificationPort, never()).sendVerificationEmail(any(), any(), any());
  }
}

