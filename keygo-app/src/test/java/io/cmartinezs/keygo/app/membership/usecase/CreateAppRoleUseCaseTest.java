package io.cmartinezs.keygo.app.membership.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.command.CreateAppRoleCommand;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.exception.ClientAppNotFoundException;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateAppRoleUseCaseTest {

  private static final String TENANT_SLUG = "keygo";
  private static final UUID CLIENT_APP_ID = UUID.randomUUID();

  @Mock private TenantRepositoryPort tenantRepositoryPort;
  @Mock private ClientAppRepositoryPort clientAppRepositoryPort;
  @Mock private AppRoleRepositoryPort appRoleRepositoryPort;

  @InjectMocks private CreateAppRoleUseCase useCase;

  @Test
  void execute_validCommand_shouldPersistRole() {
    // Given
    Tenant tenant = activeTenant();
    CreateAppRoleCommand command = new CreateAppRoleCommand(
        TENANT_SLUG, CLIENT_APP_ID, "admin", "Administrator", "Admin role");

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findAllByTenantId(tenant.getId())).thenReturn(List.of(clientApp(tenant)));
    when(appRoleRepositoryPort.existsByClientAppAndCode(CLIENT_APP_ID, RoleCode.of("admin")))
        .thenReturn(false);
    when(appRoleRepositoryPort.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    AppRole saved = useCase.execute(command);

    // Then
    assertThat(saved.getClientAppId().value()).isEqualTo(CLIENT_APP_ID);
    assertThat(saved.getCode().value()).isEqualTo("admin");
    verify(appRoleRepositoryPort).save(any());
  }

  @Test
  void execute_duplicateRoleCode_shouldThrow() {
    // Given
    Tenant tenant = activeTenant();
    CreateAppRoleCommand command = new CreateAppRoleCommand(
        TENANT_SLUG, CLIENT_APP_ID, "admin", "Administrator", null);

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findAllByTenantId(tenant.getId())).thenReturn(List.of(clientApp(tenant)));
    when(appRoleRepositoryPort.existsByClientAppAndCode(CLIENT_APP_ID, RoleCode.of("admin")))
        .thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already exists");
    verify(appRoleRepositoryPort, never()).save(any());
  }

  @Test
  void execute_appDoesNotBelongToTenant_shouldThrow() {
    // Given
    Tenant tenant = activeTenant();
    CreateAppRoleCommand command = new CreateAppRoleCommand(
        TENANT_SLUG, CLIENT_APP_ID, "admin", "Administrator", null);

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findAllByTenantId(tenant.getId())).thenReturn(List.of());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ClientAppNotFoundException.class);
    verify(appRoleRepositoryPort, never()).save(any());
  }

  @Test
  void execute_tenantNotFound_shouldThrow() {
    // Given
    CreateAppRoleCommand command = new CreateAppRoleCommand(
        TENANT_SLUG, CLIENT_APP_ID, "admin", "Administrator", null);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(TenantNotFoundException.class);
    verify(appRoleRepositoryPort, never()).save(any());
  }

  @Test
  void execute_tenantSuspended_shouldThrow() {
    // Given
    Tenant suspended = Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("KeyGo")
        .ownerEmail("admin@keygo.local")
        .status(TenantStatus.SUSPENDED)
        .build();

    CreateAppRoleCommand command = new CreateAppRoleCommand(
        TENANT_SLUG, CLIENT_APP_ID, "admin", "Administrator", null);

    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(suspended));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(TenantSuspendedException.class);
    verify(appRoleRepositoryPort, never()).save(any());
  }

  private Tenant activeTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("KeyGo")
        .ownerEmail("admin@keygo.local")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private ClientApp clientApp(Tenant tenant) {
    return ClientApp.builder()
        .id(ClientAppId.of(CLIENT_APP_ID))
        .tenantId(tenant.getId())
        .clientId(ClientId.of("keygo-ui"))
        .name("keygo-ui")
        .type(ClientType.PUBLIC)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }
}


