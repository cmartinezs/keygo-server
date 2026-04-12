package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.clientapp.port.ClientCredentialGeneratorPort;
import io.cmartinezs.keygo.app.auth.port.CredentialEncoderPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RotateClientSecretUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String CLIENT_ID = "app-abc";
  private static final String NEW_RAW_SECRET = "new-raw-secret";
  private static final String NEW_HASHED_SECRET = "$2a$hashed-new";

  @Mock private TenantRepositoryPort tenantRepositoryPort;
  @Mock private ClientAppRepositoryPort clientAppRepositoryPort;
  @Mock private ClientCredentialGeneratorPort credentialGenerator;
  @Mock private CredentialEncoderPort credentialEncoder;

  @InjectMocks
  private RotateClientSecretUseCase useCase;

  private Tenant activeTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Acme Corp")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private ClientApp confidentialApp(TenantId tenantId) {
    return ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(tenantId)
        .clientId(ClientId.of(CLIENT_ID))
        .name("App")
        .type(ClientType.CONFIDENTIAL)
        .hashedSecret("$2a$old-hashed")
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.CLIENT_CREDENTIALS), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  @Test
  void execute_validApp_shouldRotateSecret() {
    // Given
    Tenant tenant = activeTenant();
    ClientApp app = confidentialApp(tenant.getId());
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findByClientIdAndTenantId(any(), any()))
        .thenReturn(Optional.of(app));
    when(credentialGenerator.generateClientSecret()).thenReturn(NEW_RAW_SECRET);
    when(credentialEncoder.encode(NEW_RAW_SECRET)).thenReturn(NEW_HASHED_SECRET);
    when(clientAppRepositoryPort.save(any())).thenReturn(app);

    // When
    RotateSecretResult result = useCase.execute(TENANT_SLUG, CLIENT_ID);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.newRawSecret()).isEqualTo(NEW_RAW_SECRET);
  }

  @Test
  void execute_appNotFound_shouldThrow() {
    // Given
    Tenant tenant = activeTenant();
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findByClientIdAndTenantId(any(), any()))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, "missing"))
        .isInstanceOf(ClientAppNotFoundException.class);
  }
}

