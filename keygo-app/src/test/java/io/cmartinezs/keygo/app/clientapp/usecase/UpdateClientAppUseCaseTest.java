package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.app.clientapp.command.UpdateClientAppCommand;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateClientAppUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final String CLIENT_ID = "app-abc";

  @Mock private TenantRepositoryPort tenantRepositoryPort;
  @Mock private ClientAppRepositoryPort clientAppRepositoryPort;

  @InjectMocks
  private UpdateClientAppUseCase useCase;

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
        .name("Old Name")
        .type(ClientType.PUBLIC)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  @Test
  void execute_validCommand_shouldUpdateAndSave() {
    // Given
    Tenant tenant = activeTenant();
    ClientApp existing = app(tenant.getId());
    UpdateClientAppCommand command = new UpdateClientAppCommand(
        TENANT_SLUG, CLIENT_ID, "New Name", "desc",
        Set.of("https://example.com/callback"),
        Set.of(AllowedGrant.CLIENT_CREDENTIALS), Set.of("read"));

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findByClientIdAndTenantId(any(), any()))
        .thenReturn(Optional.of(existing));
    when(clientAppRepositoryPort.save(any())).thenReturn(existing);

    // When
    useCase.execute(command);

    // Then
    verify(clientAppRepositoryPort).save(any());
  }

  @Test
  void execute_appNotFound_shouldThrow() {
    // Given
    Tenant tenant = activeTenant();
    UpdateClientAppCommand command = new UpdateClientAppCommand(
        TENANT_SLUG, "missing", "New Name", null,
        Set.of(), Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of());

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findByClientIdAndTenantId(any(), any()))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ClientAppNotFoundException.class);
  }
}

