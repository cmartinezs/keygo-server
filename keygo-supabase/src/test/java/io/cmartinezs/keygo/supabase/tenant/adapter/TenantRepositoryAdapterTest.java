package io.cmartinezs.keygo.supabase.tenant.adapter;

import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.tenant.repository.TenantJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantRepositoryAdapter")
class TenantRepositoryAdapterTest {

  private static final String TEST_SLUG = "my-org";
  private static final String TEST_NAME = "Test Tenant";
  private static final String TEST_EMAIL = "owner@test.com";

  @Mock
  private TenantJpaRepository jpaRepository;

  private TenantRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new TenantRepositoryAdapter(jpaRepository);
  }

  private TenantEntity entityFor(String slug) {
    return TenantEntity.builder()
        .id(UUID.randomUUID())
        .slug(slug)
        .name(TEST_NAME)
        .ownerEmail(TEST_EMAIL)
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private Tenant domainTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TEST_SLUG))
        .name(TEST_NAME)
        .ownerEmail(TEST_EMAIL)
        .status(TenantStatus.ACTIVE)
        .build();
  }

  @Test
  @DisplayName("save should persist and return the mapped domain tenant")
  void shouldSaveAndReturnDomainTenant() {
    // Given
    Tenant tenant = domainTenant();
    TenantEntity savedEntity = entityFor(TEST_SLUG);
    when(jpaRepository.save(any())).thenReturn(savedEntity);

    // When
    Tenant result = adapter.save(tenant);

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getSlug().value()).isEqualTo(TEST_SLUG);
    verify(jpaRepository).save(any(TenantEntity.class));
  }

  @Test
  @DisplayName("findBySlug should return mapped domain tenant when entity found")
  void shouldFindBySlug() {
    // Given
    String slug = "found-tenant";
    when(jpaRepository.findBySlug(slug)).thenReturn(Optional.of(entityFor(slug)));

    // When
    Optional<Tenant> result = adapter.findBySlug(TenantSlug.of(slug));

    // Then
    assertThat(result).isPresent();
    assertThat(result.get().getSlug().value()).isEqualTo(slug);
  }

  @Test
  @DisplayName("findBySlug should return empty when entity not found")
  void shouldReturnEmptyWhenNotFound() {
    // Given
    when(jpaRepository.findBySlug("ghost")).thenReturn(Optional.empty());

    // When
    Optional<Tenant> result = adapter.findBySlug(TenantSlug.of("ghost"));

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("existsBySlug should delegate to JPA repository")
  void shouldDelegateExistsBySlug() {
    // Given
    when(jpaRepository.existsBySlug("existing")).thenReturn(true);
    when(jpaRepository.existsBySlug("missing")).thenReturn(false);

    // When / Then
    assertThat(adapter.existsBySlug(TenantSlug.of("existing"))).isTrue();
    assertThat(adapter.existsBySlug(TenantSlug.of("missing"))).isFalse();
  }
}

