package io.cmartinezs.keygo.api.discovery.controller;

import io.cmartinezs.keygo.app.clientapp.filter.ClientAppFilter;
import io.cmartinezs.keygo.app.clientapp.usecase.ListClientAppsUseCase;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.filter.TenantFilter;
import io.cmartinezs.keygo.app.tenant.usecase.GetTenantBySlugUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.ListTenantsUseCase;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.clientapp.model.RegistrationPolicy;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublicDiscoveryController")
class PublicDiscoveryControllerTest {

  @Mock private ListTenantsUseCase listTenantsUseCase;
  @Mock private GetTenantBySlugUseCase getTenantBySlugUseCase;
  @Mock private ListClientAppsUseCase listClientAppsUseCase;

  @InjectMocks
  private PublicDiscoveryController controller;

  // ─── Fixtures ─────────────────────────────────────────────────────────────

  private Tenant activeTenant(String slug) {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(slug))
        .name("Test Tenant")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private Tenant suspendedTenant(String slug) {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(slug))
        .name("Suspended Tenant")
        .status(TenantStatus.SUSPENDED)
        .build();
  }

  private ClientApp publicApp(RegistrationPolicy policy) {
    return ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(TenantId.generate())
        .clientId(ClientId.of("app-001"))
        .name("Public App")
        .description("A public app")
        .type(ClientType.PUBLIC)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .registrationPolicy(policy)
        .build();
  }

  // ─── GET /tenants/public ──────────────────────────────────────────────────

  @Test
  @DisplayName("GET /tenants/public should return 200 with active tenants")
  void listPublicTenants_activeTenants_shouldReturn200() {
    // Given
    List<Tenant> tenants = List.of(activeTenant("alpha"), activeTenant("beta"));
    when(listTenantsUseCase.execute(any(TenantFilter.class)))
        .thenReturn(PagedResult.of(tenants, 0, 50, 2L));

    // When
    var response = controller.listPublicTenants(null, 0, 50, null, null);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().getContent()).hasSize(2);
    assertThat(response.getBody().getData().getTotalElements()).isEqualTo(2L);
    assertThat(response.getBody().getSuccess().getCode()).isEqualTo("TENANT_LIST_RETRIEVED");
  }

  @Test
  @DisplayName("GET /tenants/public always forces status=ACTIVE filter")
  void listPublicTenants_shouldForceActiveStatusFilter() {
    // Given
    when(listTenantsUseCase.execute(any(TenantFilter.class)))
        .thenReturn(PagedResult.of(List.of(), 0, 50, 0L));

    // When
    controller.listPublicTenants(null, 0, 50, null, null);

    // Then — filter must always have status=ACTIVE regardless of caller
    verify(listTenantsUseCase).execute(argThat((TenantFilter f) ->
        f.hasStatus() && f.getStatus() == TenantStatus.ACTIVE));
  }

  @Test
  @DisplayName("GET /tenants/public?name_like=acme should pass nameLike filter")
  void listPublicTenants_withNameLike_shouldPassFilter() {
    // Given
    when(listTenantsUseCase.execute(any(TenantFilter.class)))
        .thenReturn(PagedResult.of(List.of(), 0, 50, 0L));

    // When
    controller.listPublicTenants("acme", 0, 50, null, null);

    // Then
    verify(listTenantsUseCase).execute(argThat((TenantFilter f) ->
        f.hasNameLike() && "acme".equals(f.getNameLike())));
  }

  @Test
  @DisplayName("GET /tenants/public should return empty page when no tenants found")
  void listPublicTenants_empty_shouldReturn200WithEmptyContent() {
    // Given
    when(listTenantsUseCase.execute(any(TenantFilter.class)))
        .thenReturn(PagedResult.of(List.of(), 0, 50, 0L));

    // When
    var response = controller.listPublicTenants(null, 0, 50, null, null);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getContent()).isEmpty();
    assertThat(response.getBody().getData().getTotalElements()).isZero();
  }

  @Test
  @DisplayName("GET /tenants/public maps tenant fields to TenantPublicData correctly")
  void listPublicTenants_shouldMapFieldsCorrectly() {
    // Given
    Tenant tenant = activeTenant("my-company");
    when(listTenantsUseCase.execute(any(TenantFilter.class)))
        .thenReturn(PagedResult.of(List.of(tenant), 0, 50, 1L));

    // When
    var response = controller.listPublicTenants(null, 0, 50, null, null);

    // Then
    var data = response.getBody().getData().getContent().get(0);
    assertThat(data.getName()).isEqualTo("Test Tenant");
    assertThat(data.getSlug()).isEqualTo("my-company");
    assertThat(data.getId()).isNotNull();
  }

  // ─── GET /tenants/{tenantSlug}/apps/public ────────────────────────────────

  @Test
  @DisplayName("GET /tenants/{slug}/apps/public should return 200 with public apps")
  void listPublicClientApps_activeTenant_shouldReturn200() {
    // Given
    Tenant tenant = activeTenant("acme");
    ClientApp app = publicApp(RegistrationPolicy.OPEN_AUTO_ACTIVE);
    when(getTenantBySlugUseCase.execute("acme")).thenReturn(tenant);
    when(listClientAppsUseCase.execute(eq("acme"), any(ClientAppFilter.class)))
        .thenReturn(PagedResult.of(List.of(app), 0, 50, 1L));

    // When
    var response = controller.listPublicClientApps("acme", 0, 50, null, null);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getContent()).hasSize(1);
    assertThat(response.getBody().getSuccess().getCode()).isEqualTo("CLIENT_APP_LIST_RETRIEVED");
  }

  @Test
  @DisplayName("GET /tenants/{slug}/apps/public forces type=PUBLIC and excludes INVITE_ONLY")
  void listPublicClientApps_shouldForceMandatoryFilters() {
    // Given
    Tenant tenant = activeTenant("acme");
    when(getTenantBySlugUseCase.execute("acme")).thenReturn(tenant);
    when(listClientAppsUseCase.execute(eq("acme"), any(ClientAppFilter.class)))
        .thenReturn(PagedResult.of(List.of(), 0, 50, 0L));

    // When
    controller.listPublicClientApps("acme", 0, 50, null, null);

    // Then
    verify(listClientAppsUseCase).execute(eq("acme"), argThat((ClientAppFilter f) ->
        f.hasType() && f.getType() == ClientType.PUBLIC
        && f.hasExcludeRegistrationPolicies()
        && f.getExcludeRegistrationPolicies().contains(RegistrationPolicy.INVITE_ONLY)
        && f.getExcludeRegistrationPolicies().contains(RegistrationPolicy.OPEN_NO_MEMBERSHIP)
        && f.hasStatus() && f.getStatus() == ClientAppStatus.ACTIVE));
  }

  @Test
  @DisplayName("GET /tenants/{slug}/apps/public should throw TenantNotFoundException when tenant not found")
  void listPublicClientApps_tenantNotFound_shouldPropagate() {
    // Given
    when(getTenantBySlugUseCase.execute("ghost")).thenThrow(new TenantNotFoundException("ghost"));

    // When / Then
    assertThatThrownBy(() -> controller.listPublicClientApps("ghost", 0, 50, null, null))
        .isInstanceOf(TenantNotFoundException.class);
  }

  @Test
  @DisplayName("GET /tenants/{slug}/apps/public should throw TenantNotFoundException when tenant is SUSPENDED")
  void listPublicClientApps_suspendedTenant_shouldThrowNotFound() {
    // Given
    when(getTenantBySlugUseCase.execute("suspended")).thenReturn(suspendedTenant("suspended"));

    // When / Then
    assertThatThrownBy(() -> controller.listPublicClientApps("suspended", 0, 50, null, null))
        .isInstanceOf(TenantNotFoundException.class);
  }

  @Test
  @DisplayName("GET /tenants/{slug}/apps/public maps ClientApp fields to ClientAppPublicData correctly")
  void listPublicClientApps_shouldMapFieldsCorrectly() {
    // Given
    Tenant tenant = activeTenant("acme");
    ClientApp app = publicApp(RegistrationPolicy.OPEN_AUTO_PENDING);
    when(getTenantBySlugUseCase.execute("acme")).thenReturn(tenant);
    when(listClientAppsUseCase.execute(eq("acme"), any(ClientAppFilter.class)))
        .thenReturn(PagedResult.of(List.of(app), 0, 50, 1L));

    // When
    var response = controller.listPublicClientApps("acme", 0, 50, null, null);

    // Then
    var data = response.getBody().getData().getContent().get(0);
    assertThat(data.getClientId()).isEqualTo("app-001");
    assertThat(data.getName()).isEqualTo("Public App");
    assertThat(data.getDescription()).isEqualTo("A public app");
    assertThat(data.getType()).isEqualTo("PUBLIC");
    assertThat(data.getRegistrationPolicy()).isEqualTo("OPEN_AUTO_PENDING");
    assertThat(data.isActive()).isTrue();
  }

  @Test
  @DisplayName("GET /tenants/{slug}/apps/public returns empty page when no matching apps")
  void listPublicClientApps_noApps_shouldReturn200WithEmptyContent() {
    // Given
    Tenant tenant = activeTenant("acme");
    when(getTenantBySlugUseCase.execute("acme")).thenReturn(tenant);
    when(listClientAppsUseCase.execute(eq("acme"), any(ClientAppFilter.class)))
        .thenReturn(PagedResult.of(List.of(), 0, 50, 0L));

    // When
    var response = controller.listPublicClientApps("acme", 0, 50, null, null);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getContent()).isEmpty();
  }
}
