package io.cmartinezs.keygo.app.tenant.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivateTenantUseCase")
class ActivateTenantUseCaseTest {

  @Mock
  private TenantRepositoryPort tenantRepositoryPort;

  @InjectMocks
  private ActivateTenantUseCase useCase;

  private Tenant suspendedTenant(String slug) {
    Tenant t = Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(slug))
        .name("My Tenant")
        .status(TenantStatus.SUSPENDED)
        .build();
    return t;
  }

  @Test
  @DisplayName("should activate a suspended tenant and persist the change")
  void shouldActivateSuspendedTenant() {
    // Given
    String slug = "my-tenant";
    Tenant tenant = suspendedTenant(slug);
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(slug))).thenReturn(Optional.of(tenant));
    when(tenantRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    Tenant result = useCase.execute(slug);

    // Then
    assertThat(result.isActive()).isTrue();
    assertThat(result.getStatus()).isEqualTo(TenantStatus.ACTIVE);
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
  @DisplayName("should activate a PENDING tenant")
  void shouldActivatePendingTenant() {
    // Given
    String slug = "pending-tenant";
    Tenant tenant = Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(slug))
        .name("Pending Tenant")
        .status(TenantStatus.PENDING)
        .build();
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(slug))).thenReturn(Optional.of(tenant));
    when(tenantRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // When
    Tenant result = useCase.execute(slug);

    // Then
    assertThat(result.isActive()).isTrue();
    assertThat(result.getStatus()).isEqualTo(TenantStatus.ACTIVE);
  }
}

