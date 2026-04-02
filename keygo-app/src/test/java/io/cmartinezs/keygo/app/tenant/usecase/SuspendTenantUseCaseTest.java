package io.cmartinezs.keygo.app.tenant.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuspendTenantUseCase")
class SuspendTenantUseCaseTest {

  @Mock
  private TenantRepositoryPort tenantRepositoryPort;

  @InjectMocks
  private SuspendTenantUseCase useCase;

  private Tenant activeTenant(String slug) {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(slug))
        .name("My Tenant")
        .ownerEmail("owner@example.com")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  @Test
  @DisplayName("should suspend an active tenant and persist the change")
  void shouldSuspendActiveTenant() {
    // Given
    String slug = "my-tenant";
    Tenant tenant = activeTenant(slug);
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(slug))).thenReturn(Optional.of(tenant));
    when(tenantRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    Tenant result = useCase.execute(slug);

    // Then
    assertThat(result.isSuspended()).isTrue();
    assertThat(result.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
    verify(tenantRepositoryPort).save(tenant);
  }

  @Test
  @DisplayName("should throw TenantNotFoundException when tenant does not exist")
  void shouldThrowWhenNotFound() {
    // Given
    String slug = "missing-tenant";
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(slug))).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(slug))
        .isInstanceOf(TenantNotFoundException.class)
        .hasMessageContaining(slug);

    verify(tenantRepositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("should throw TenantSuspendedException when tenant is already suspended")
  void shouldThrowWhenAlreadySuspended() {
    // Given
    String slug = "already-suspended";
    Tenant tenant = activeTenant(slug);
    tenant.suspend(); // already suspended
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(slug))).thenReturn(Optional.of(tenant));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(slug))
        .isInstanceOf(TenantSuspendedException.class)
        .hasMessageContaining("suspended");

    verify(tenantRepositoryPort, never()).save(any());
  }
}

