package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.app.clientapp.filter.ClientAppFilter;
import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListClientAppsUseCaseTest {

  private static final String TENANT_SLUG = "acme";

  @Mock private TenantRepositoryPort tenantRepositoryPort;
  @Mock private ClientAppRepositoryPort clientAppRepositoryPort;

  @InjectMocks
  private ListClientAppsUseCase useCase;

  private Tenant activeTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Acme Corp")
        .ownerEmail("owner@acme.com")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private ClientApp sampleApp(TenantId tenantId) {
    return ClientApp.builder()
        .id(ClientAppId.generate())
        .tenantId(tenantId)
        .clientId(ClientId.of("app-client-id"))
        .name("App")
        .type(ClientType.PUBLIC)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  @Test
  void execute_existingTenantWithApps_shouldReturnList() {
    // Given
    Tenant tenant = activeTenant();
    List<ClientApp> apps = List.of(sampleApp(tenant.getId()), sampleApp(tenant.getId()));
    PagedResult<ClientApp> pagedResult = PagedResult.of(apps, 0, 20, 2);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findAllPaged(eq(tenant.getId()), any()))
        .thenReturn(pagedResult);

    // When
    ClientAppFilter filter = ClientAppFilter.of(null, null, 0, 20, null, null);
    PagedResult<ClientApp> result = useCase.execute(TENANT_SLUG, filter);

    // Then
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2);
  }

  @Test
  void execute_existingTenantWithNoApps_shouldReturnEmptyList() {
    // Given
    Tenant tenant = activeTenant();
    PagedResult<ClientApp> pagedResult = PagedResult.of(List.of(), 0, 20, 0);
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.of(tenant));
    when(clientAppRepositoryPort.findAllPaged(eq(tenant.getId()), any()))
        .thenReturn(pagedResult);

    // When
    ClientAppFilter filter = ClientAppFilter.of(null, null, 0, 20, null, null);
    PagedResult<ClientApp> result = useCase.execute(TENANT_SLUG, filter);

    // Then
    assertThat(result.getContent()).isEmpty();
  }

  @Test
  void execute_tenantNotFound_shouldThrow() {
    // Given
    when(tenantRepositoryPort.findBySlug(any())).thenReturn(Optional.empty());

    // When / Then
    ClientAppFilter filter = ClientAppFilter.of(null, null, 0, 20, null, null);
    assertThatThrownBy(() -> useCase.execute("unknown", filter))
        .isInstanceOf(TenantNotFoundException.class);
  }
}

