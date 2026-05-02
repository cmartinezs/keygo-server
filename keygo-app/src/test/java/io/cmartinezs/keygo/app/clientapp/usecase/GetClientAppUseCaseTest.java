package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
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
class GetClientAppUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String CLIENT_ID = "app-abc";

  @Mock private TenantRepositoryPort tenantRepositoryPort;
  @Mock private ClientAppRepositoryPort clientAppRepositoryPort;

  @InjectMocks
  private GetClientAppUseCase useCase;

  private Tenant activeTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Acme Corp")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private ClientApp app(TenantId tenantId) {
    return ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(tenantId)
        .clientId(ClientId.of(CLIENT_ID))
        .name("App")
        .type(ClientType.PUBLIC)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  @Test
  void execute_appExists_shouldReturnApp() {
    // Given
    Tenant tenant = activeTenant();
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findByClientIdAndTenantId(any(), any()))
        .thenReturn(Optional.of(app(tenant.getId())));

    // When
    ClientApp result = useCase.execute(TENANT_SLUG, CLIENT_ID);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getClientId().value()).isEqualTo(CLIENT_ID);
  }

  @Test
  void execute_appNotFound_shouldThrow() {
    // Given
    Tenant tenant = activeTenant();
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findByClientIdAndTenantId(any(), any()))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, CLIENT_ID))
        .isInstanceOf(ClientAppNotFoundException.class);
  }

  @Test
  void execute_tenantNotFound_shouldThrow() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute("unknown", CLIENT_ID))
        .isInstanceOf(TenantNotFoundException.class);
  }
}

