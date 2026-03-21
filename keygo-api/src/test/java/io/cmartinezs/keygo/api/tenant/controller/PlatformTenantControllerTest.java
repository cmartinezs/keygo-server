package io.cmartinezs.keygo.api.tenant.controller;

import io.cmartinezs.keygo.api.tenant.request.CreateTenantRequest;
import io.cmartinezs.keygo.app.tenant.command.CreateTenantCommand;
import io.cmartinezs.keygo.app.tenant.usecase.CreateTenantUseCase;
import io.cmartinezs.keygo.app.tenant.usecase.GetTenantBySlugUseCase;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformTenantController")
class PlatformTenantControllerTest {

  @Mock private CreateTenantUseCase createTenantUseCase;
  @Mock private GetTenantBySlugUseCase getTenantBySlugUseCase;
  @Mock private SuspendTenantUseCase suspendTenantUseCase;

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
        new CreateTenantRequest(
            "Test Tenant", "test-tenant", "owner@test.com");
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
}

