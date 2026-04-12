package io.cmartinezs.keygo.app.tenant.usecase;

import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetTenantBySlugUseCase")
class GetTenantBySlugUseCaseTest {

  @Mock
  private TenantRepositoryPort tenantRepositoryPort;

  @InjectMocks
  private GetTenantBySlugUseCase useCase;

  @Test
  @DisplayName("should return tenant when found by slug")
  void shouldReturnTenantWhenFound() {
    // Given
    String slug = "my-tenant";
    Tenant tenant = Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(slug))
        .name("My Tenant")
        .status(TenantStatus.ACTIVE)
        .build();
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(slug))).thenReturn(Optional.of(tenant));

    // When
    Tenant result = useCase.execute(slug);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getSlug().value()).isEqualTo(slug);
  }

  @Test
  @DisplayName("should throw TenantNotFoundException when slug not found")
  void shouldThrowWhenNotFound() {
    // Given
    String slug = "unknown-tenant";
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(slug))).thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(slug))
        .isInstanceOf(TenantNotFoundException.class)
        .hasMessageContaining(slug);
  }
}

