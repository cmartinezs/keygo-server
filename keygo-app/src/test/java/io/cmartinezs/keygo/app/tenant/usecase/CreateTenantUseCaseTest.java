package io.cmartinezs.keygo.app.tenant.usecase;

import io.cmartinezs.keygo.app.tenant.command.CreateTenantCommand;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateTenantUseCase")
class CreateTenantUseCaseTest {

  private static final String TEST_SLUG = "my-tenant";
  private static final String TEST_NAME = "My Tenant";
  private static final String TEST_EMAIL = "owner@example.com";

  @Mock
  private TenantRepositoryPort tenantRepositoryPort;

  @InjectMocks
  private CreateTenantUseCase useCase;

  private Tenant buildTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TEST_SLUG))
        .name(TEST_NAME)
        .ownerEmail(TEST_EMAIL)
        .status(TenantStatus.ACTIVE)
        .build();
  }

  @Test
  @DisplayName("should create and persist a new tenant")
  void shouldCreateTenant() {
    // Given
    CreateTenantCommand command = new CreateTenantCommand(TEST_NAME, TEST_SLUG, TEST_EMAIL);
    Tenant savedTenant = buildTenant();
    when(tenantRepositoryPort.existsBySlug(any())).thenReturn(false);
    when(tenantRepositoryPort.save(any())).thenReturn(savedTenant);

    // When
    Tenant result = useCase.execute(command);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getSlug().value()).isEqualTo(TEST_SLUG);
    assertThat(result.isActive()).isTrue();
    verify(tenantRepositoryPort).save(any(Tenant.class));
  }

  @Test
  @DisplayName("should reject command when slug is already taken")
  void shouldRejectDuplicateSlug() {
    // Given
    String existingSlug = "existing-slug";
    CreateTenantCommand command = new CreateTenantCommand(TEST_NAME, existingSlug, TEST_EMAIL);
    when(tenantRepositoryPort.existsBySlug(TenantSlug.of(existingSlug))).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining(existingSlug);

    verify(tenantRepositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("should reject command with invalid slug")
  void shouldRejectInvalidSlug() {
    // Given
    CreateTenantCommand command = new CreateTenantCommand(TEST_NAME, "-bad-slug", TEST_EMAIL);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class);

    verify(tenantRepositoryPort, never()).save(any());
  }
}

