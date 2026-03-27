package io.cmartinezs.keygo.api.tenant.controller;

import io.cmartinezs.keygo.api.shared.response.PagedData;
import io.cmartinezs.keygo.api.tenant.request.CreateTenantRequest;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.tenant.command.CreateTenantCommand;
import io.cmartinezs.keygo.app.tenant.filter.TenantFilter;
import io.cmartinezs.keygo.app.tenant.usecase.CreateTenantUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.GetTenantBySlugUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.ListTenantsUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.SuspendTenantUseCase;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformTenantController")
class PlatformTenantControllerTest {

  @Mock private CreateTenantUseCase createTenantUseCase;
  @Mock private GetTenantBySlugUseCase getTenantBySlugUseCase;
  @Mock private SuspendTenantUseCase suspendTenantUseCase;
  @Mock private ListTenantsUseCase listTenantsUseCase;

  @InjectMocks
  private PlatformTenantController controller;

  private Tenant buildTenant(String slug, TenantStatus status) {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(slug))
        .name("Test Tenant")
        .ownerEmail("owner@test.com")
        .status(status)
        .build();
  }

  @Test
  @DisplayName("POST /tenants should return 201 with tenant data")
  void shouldCreateTenantAndReturn201() {
    // Given
    CreateTenantRequest request =
        new CreateTenantRequest("Test Tenant", "owner@test.com");
    Tenant created = buildTenant("test-tenant", TenantStatus.ACTIVE);
    when(createTenantUseCase.execute(any(CreateTenantCommand.class))).thenReturn(created);

    // When
    var response = controller.createTenant(request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData()).isNotNull();
    assertThat(response.getBody().getData().getSlug()).isEqualTo("test-tenant");
    assertThat(response.getBody().getData().getStatus()).isEqualTo("ACTIVE");
  }

  @Test
  @DisplayName("GET /tenants/{slug} should return 200 with tenant data")
  void shouldGetTenantBySlugAndReturn200() {
    // Given
    Tenant tenant = buildTenant("my-tenant", TenantStatus.ACTIVE);
    when(getTenantBySlugUseCase.execute("my-tenant")).thenReturn(tenant);

    // When
    var response = controller.getTenantBySlug("my-tenant");

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().getSlug()).isEqualTo("my-tenant");
  }

  @Test
  @DisplayName("GET /tenants/{slug} should propagate TenantNotFoundException")
  void shouldPropagateTenantNotFound() {
    // Given
    when(getTenantBySlugUseCase.execute("ghost")).thenThrow(new TenantNotFoundException("ghost"));

    // When / Then
    assertThatThrownBy(() -> controller.getTenantBySlug("ghost"))
        .isInstanceOf(TenantNotFoundException.class);
  }

  @Test
  @DisplayName("PUT /tenants/{slug}/suspend should return 200 with suspended tenant")
  void shouldSuspendTenantAndReturn200() {
    // Given
    Tenant suspended = buildTenant("my-tenant", TenantStatus.SUSPENDED);
    when(suspendTenantUseCase.execute("my-tenant")).thenReturn(suspended);

    // When
    var response = controller.suspendTenant("my-tenant");

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().getStatus()).isEqualTo("SUSPENDED");
  }

  // ─── GET /tenants (list) ──────────────────────────────────────────────────

  @Test
  @DisplayName("GET /tenants should return 200 with paginated tenant list")
  void shouldListTenantsAndReturn200() {
    // Given
    List<Tenant> tenants = List.of(
        buildTenant("alpha", TenantStatus.ACTIVE),
        buildTenant("beta", TenantStatus.ACTIVE)
    );
    PagedResult<Tenant> pagedResult = PagedResult.of(tenants, 0, 20, 2L);
    when(listTenantsUseCase.execute(any(TenantFilter.class))).thenReturn(pagedResult);

    // When
    var response = controller.listTenants(null, null, 0, 20);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    PagedData<?> data = response.getBody().getData();
    assertThat(data).isNotNull();
    assertThat(data.getContent()).hasSize(2);
    assertThat(data.getTotalElements()).isEqualTo(2L);
    assertThat(data.getPage()).isZero();
    assertThat(data.getTotalPages()).isEqualTo(1);
    assertThat(data.isLast()).isTrue();
  }

  @Test
  @DisplayName("GET /tenants?status=ACTIVE should pass filter to use case")
  void shouldFilterByStatus() {
    // Given
    List<Tenant> active = List.of(buildTenant("active-one", TenantStatus.ACTIVE));
    PagedResult<Tenant> pagedResult = PagedResult.of(active, 0, 20, 1L);
    when(listTenantsUseCase.execute(any(TenantFilter.class))).thenReturn(pagedResult);

    // When
    var response = controller.listTenants(TenantStatus.ACTIVE, null, 0, 20);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getContent()).hasSize(1);
    verify(listTenantsUseCase).execute(argThat(f ->
        f.hasStatus() && f.getStatus() == TenantStatus.ACTIVE));
  }

  @Test
  @DisplayName("GET /tenants?name_like=acme should pass nameLike filter to use case")
  void shouldFilterByNameLike() {
    // Given
    when(listTenantsUseCase.execute(any(TenantFilter.class)))
        .thenReturn(PagedResult.of(List.of(), 0, 20, 0L));

    // When
    controller.listTenants(null, "acme", 0, 20);

    // Then
    verify(listTenantsUseCase).execute(argThat(f ->
        f.hasNameLike() && "acme".equals(f.getNameLike())));
  }

  @Test
  @DisplayName("GET /tenants should return empty page when no tenants found")
  void shouldReturnEmptyPageWhenNoTenantsFound() {
    // Given
    when(listTenantsUseCase.execute(any(TenantFilter.class)))
        .thenReturn(PagedResult.of(List.of(), 0, 20, 0L));

    // When
    var response = controller.listTenants(null, null, 0, 20);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getContent()).isEmpty();
    assertThat(response.getBody().getData().getTotalElements()).isZero();
  }

  @Test
  @DisplayName("GET /tenants should propagate IllegalArgumentException on invalid page params")
  void shouldPropagateIllegalArgumentExceptionOnInvalidParams() {
    // When / Then
    assertThatThrownBy(() -> controller.listTenants(null, null, -1, 20))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Page must be >= 0");
  }
}


