package io.cmartinezs.keygo.app.tenant.usecase;

import io.cmartinezs.keygo.app.tenant.command.CreateTenantCommand;
import io.cmartinezs.keygo.app.tenant.exception.DuplicateTenantException;
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

  private static final String TEST_NAME = "My Tenant";
  /* Slug derived from TEST_NAME via SlugUtils.toSlug() */
  private static final String EXPECTED_SLUG = "my-tenant";
  private static final String TEST_EMAIL = "owner@example.com";

  @Mock
  private TenantRepositoryPort tenantRepositoryPort;

  @InjectMocks
  private CreateTenantUseCase useCase;

  private Tenant buildTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(EXPECTED_SLUG))
        .name(TEST_NAME)
        .ownerEmail(TEST_EMAIL)
        .status(TenantStatus.ACTIVE)
        .build();
  }

  @Test
  @DisplayName("should create and persist a new tenant, deriving the slug from the name")
  void shouldCreateTenant() {
    // Given
    CreateTenantCommand command = new CreateTenantCommand(TEST_NAME, TEST_EMAIL);
    Tenant savedTenant = buildTenant();
    when(tenantRepositoryPort.existsBySlug(any())).thenReturn(false);
    when(tenantRepositoryPort.save(any())).thenReturn(savedTenant);

    // When
    Tenant result = useCase.execute(command);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getSlug().value()).isEqualTo(EXPECTED_SLUG);
    assertThat(result.isActive()).isTrue();
    verify(tenantRepositoryPort).save(any(Tenant.class));
  }

  @Test
  @DisplayName("should reject command when derived slug is already taken")
  void shouldRejectDuplicateSlug() {
    // Given — "Existing Slug" → slug "existing-slug"
    CreateTenantCommand command = new CreateTenantCommand("Existing Slug", TEST_EMAIL);
    when(tenantRepositoryPort.existsBySlug(any())).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(DuplicateTenantException.class)
        .hasMessageContaining("existing-slug");

    verify(tenantRepositoryPort, never()).save(any());
  }

  @Test
  @DisplayName("should reject command when name produces an invalid slug (too short)")
  void shouldRejectNameThatProducesTooShortSlug() {
    // Given — single letter name → slug "a" → below 3-char minimum
    CreateTenantCommand command = new CreateTenantCommand("a", TEST_EMAIL);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class);

    verify(tenantRepositoryPort, never()).save(any());
  }
}



