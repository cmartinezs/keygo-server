package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.app.clientapp.command.CreateClientAppCommand;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientCredentialGeneratorPort;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.InvalidClientAppConfigException;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateClientAppUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String APP_NAME = "My App";
  private static final String RAW_CLIENT_ID = "abc123";
  private static final String RAW_SECRET = "rawsecret";
  private static final String HASHED_SECRET = "$2a$hashed";

  @Mock private TenantRepositoryPort tenantRepositoryPort;
  @Mock private ClientAppRepositoryPort clientAppRepositoryPort;
  @Mock private ClientCredentialGeneratorPort credentialGenerator;
  @Mock private CredentialEncoderPort credentialEncoder;

  @InjectMocks
  private CreateClientAppUseCase useCase;

  private Tenant activeTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Acme Corp")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private ClientApp savedApp(Tenant tenant) {
    return ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(tenant.getId())
        .clientId(ClientId.of(RAW_CLIENT_ID))
        .name(APP_NAME)
        .type(ClientType.CONFIDENTIAL)
        .hashedSecret(HASHED_SECRET)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  @Test
  void execute_validCommand_shouldCreateClientApp() {
    // Given
    Tenant tenant = activeTenant();
    CreateClientAppCommand command = new CreateClientAppCommand(
        TENANT_SLUG, APP_NAME, null, ClientType.CONFIDENTIAL,
        Set.of("https://example.com/callback"), Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of());

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(credentialGenerator.generateClientId()).thenReturn(RAW_CLIENT_ID);
    when(credentialGenerator.generateClientSecret()).thenReturn(RAW_SECRET);
    when(credentialEncoder.encode(RAW_SECRET)).thenReturn(HASHED_SECRET);
    when(clientAppRepositoryPort.existsByClientId(any())).thenReturn(false);
    when(clientAppRepositoryPort.save(any())).thenReturn(savedApp(tenant));

    // When
    CreateClientAppResult result = useCase.execute(command);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.app()).isNotNull();
    assertThat(result.rawSecret()).isEqualTo(RAW_SECRET);
    verify(clientAppRepositoryPort).save(any());
  }

  @Test
  void execute_publicClientApp_shouldNotGenerateSecret() {
    // Given
    Tenant tenant = activeTenant();
    CreateClientAppCommand command = new CreateClientAppCommand(
        TENANT_SLUG, APP_NAME, null, ClientType.PUBLIC,
        Set.of("https://example.com/callback"), Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of());

    ClientApp publicApp = ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(tenant.getId())
        .clientId(ClientId.of(RAW_CLIENT_ID))
        .name(APP_NAME)
        .type(ClientType.PUBLIC)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(credentialGenerator.generateClientId()).thenReturn(RAW_CLIENT_ID);
    when(clientAppRepositoryPort.existsByClientId(any())).thenReturn(false);
    when(clientAppRepositoryPort.save(any())).thenReturn(publicApp);

    // When
    CreateClientAppResult result = useCase.execute(command);

    // Then
    assertThat(result.rawSecret()).isNull();
    verify(credentialEncoder, never()).encode(any());
  }

  @Test
  void execute_tenantNotFound_shouldThrow() {
    // Given
    CreateClientAppCommand command = new CreateClientAppCommand(
        "unknown", APP_NAME, null, ClientType.PUBLIC,
        Set.of(), Set.of(AllowedGrant.CLIENT_CREDENTIALS), Set.of());
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(TenantNotFoundException.class);
    verify(clientAppRepositoryPort, never()).save(any());
  }

  @Test
  void execute_tenantSuspended_shouldThrow() {
    // Given
    Tenant suspended = Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Acme Corp")
        .status(TenantStatus.SUSPENDED)
        .build();
    CreateClientAppCommand command = new CreateClientAppCommand(
        TENANT_SLUG, APP_NAME, null, ClientType.PUBLIC,
        Set.of(), Set.of(AllowedGrant.CLIENT_CREDENTIALS), Set.of());
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(suspended));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(TenantSuspendedException.class);
    verify(clientAppRepositoryPort, never()).save(any());
  }

  @Test
  void execute_authorizationCodeWithoutRedirectUri_shouldThrow() {
    // Given
    Tenant tenant = activeTenant();
    CreateClientAppCommand command = new CreateClientAppCommand(
        TENANT_SLUG, APP_NAME, null, ClientType.CONFIDENTIAL,
        null, Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of());
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(InvalidClientAppConfigException.class)
        .hasMessageContaining("AUTHORIZATION_CODE");
    verify(clientAppRepositoryPort, never()).save(any());
  }

  @Test
  void execute_publicAppWithClientCredentials_shouldThrow() {
    // Given
    Tenant tenant = activeTenant();
    CreateClientAppCommand command = new CreateClientAppCommand(
        TENANT_SLUG, APP_NAME, null, ClientType.PUBLIC,
        Set.of(), Set.of(AllowedGrant.CLIENT_CREDENTIALS), Set.of());
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(InvalidClientAppConfigException.class)
        .hasMessageContaining("CLIENT_CREDENTIALS");
    verify(clientAppRepositoryPort, never()).save(any());
  }
}

