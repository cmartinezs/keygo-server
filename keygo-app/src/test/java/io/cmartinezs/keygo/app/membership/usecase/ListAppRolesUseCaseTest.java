package io.cmartinezs.keygo.app.membership.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
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
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListAppRolesUseCase")
class ListAppRolesUseCaseTest {

  private static final String TENANT_SLUG = "acme-corp";
  private static final UUID CLIENT_APP_UUID = UUID.randomUUID();

  @Mock private TenantRepositoryPort tenantRepositoryPort;
  @Mock private ClientAppRepositoryPort clientAppRepositoryPort;
  @Mock private AppRoleRepositoryPort appRoleRepositoryPort;

  @InjectMocks private ListAppRolesUseCase useCase;

  @Test
  @DisplayName("should return roles when tenant and app are valid")
  void execute_validTenantAndApp_shouldReturnRoles() {
    // Given
    Tenant tenant = activeTenant();
    ClientApp clientApp = clientAppForTenant(tenant.getId(), CLIENT_APP_UUID);
    List<AppRole> roles = List.of(appRole("admin"), appRole("user"));

    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findAllByTenantId(tenant.getId())).thenReturn(List.of(clientApp));
    when(appRoleRepositoryPort.findByClientAppId(CLIENT_APP_UUID)).thenReturn(roles);

    // When
    List<AppRole> result = useCase.execute(TENANT_SLUG, CLIENT_APP_UUID);

    // Then
    assertThat(result).hasSize(2);
    verify(appRoleRepositoryPort).findByClientAppId(CLIENT_APP_UUID);
  }

  @Test
  @DisplayName("should throw TenantNotFoundException when tenant does not exist")
  void execute_tenantNotFound_shouldThrow() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, CLIENT_APP_UUID))
        .isInstanceOf(TenantNotFoundException.class)
        .hasMessageContaining(TENANT_SLUG);

    verify(clientAppRepositoryPort, never()).findAllByTenantId(any());
    verify(appRoleRepositoryPort, never()).findByClientAppId(any());
  }

  @Test
  @DisplayName("should throw TenantSuspendedException when tenant is suspended")
  void execute_tenantSuspended_shouldThrow() {
    // Given
    Tenant suspendedTenant = suspendedTenant();
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(suspendedTenant));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, CLIENT_APP_UUID))
        .isInstanceOf(TenantSuspendedException.class);

    verify(clientAppRepositoryPort, never()).findAllByTenantId(any());
    verify(appRoleRepositoryPort, never()).findByClientAppId(any());
  }

  @Test
  @DisplayName("should throw ClientAppNotFoundException when app does not belong to tenant")
  void execute_appNotBelongingToTenant_shouldThrow() {
    // Given
    Tenant tenant = activeTenant();
    UUID otherAppUuid = UUID.randomUUID();
    ClientApp otherApp = clientAppForTenant(tenant.getId(), otherAppUuid); // different app

    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findAllByTenantId(tenant.getId())).thenReturn(List.of(otherApp));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, CLIENT_APP_UUID))
        .isInstanceOf(ClientAppNotFoundException.class)
        .hasMessageContaining(CLIENT_APP_UUID.toString());

    verify(appRoleRepositoryPort, never()).findByClientAppId(any());
  }

  @Test
  @DisplayName("should throw ClientAppNotFoundException when tenant has no apps at all")
  void execute_tenantHasNoApps_shouldThrow() {
    // Given
    Tenant tenant = activeTenant();
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG))).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findAllByTenantId(tenant.getId())).thenReturn(List.of());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, CLIENT_APP_UUID))
        .isInstanceOf(ClientAppNotFoundException.class);

    verify(appRoleRepositoryPort, never()).findByClientAppId(any());
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────

  private Tenant activeTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Acme Corp")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private Tenant suspendedTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Acme Corp")
        .status(TenantStatus.SUSPENDED)
        .build();
  }

  private ClientApp clientAppForTenant(TenantId tenantId, UUID appUuid) {
    return ClientApp.builder()
        .id(ClientAppId.of(appUuid))
        .tenantId(tenantId)
        .clientId(ClientId.of("acme-app-" + appUuid.toString().substring(0, 8)))
        .name("Acme App")
        .type(ClientType.PUBLIC)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  private AppRole appRole(String code) {
    return AppRole.builder()
        .id(AppRoleId.generate())
        .clientAppId(ClientAppId.of(CLIENT_APP_UUID))
        .code(RoleCode.of(code))
        .displayName(code)
        .description(code + " role")
        .build();
  }
}




