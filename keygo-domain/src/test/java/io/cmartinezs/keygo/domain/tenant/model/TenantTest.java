package io.cmartinezs.keygo.domain.tenant.model;

import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Tenant domain entity.
 */
@DisplayName("Tenant")
class TenantTest {

  private static final String VALID_SLUG = "my-tenant";
  private static final String VALID_NAME = "My Tenant";
  private static final String VALID_EMAIL = "owner@example.com";

  private Tenant validTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(VALID_SLUG))
        .name(VALID_NAME)
        .ownerEmail(VALID_EMAIL)
        .status(TenantStatus.ACTIVE)
        .build();
  }

  @Test
  @DisplayName("should create an active tenant")
  void shouldCreateActiveTenant() {
    // When
    Tenant tenant = validTenant();

    // Then
    assertThat(tenant.isActive()).isTrue();
    assertThat(tenant.isSuspended()).isFalse();
    assertThat(tenant.getSlug().value()).isEqualTo(VALID_SLUG);
    assertThat(tenant.getName()).isEqualTo(VALID_NAME);
    assertThat(tenant.getOwnerEmail()).isEqualTo(VALID_EMAIL);
  }

  @Test
  @DisplayName("should suspend an active tenant")
  void shouldSuspendActiveTenant() {
    // Given
    Tenant tenant = validTenant();

    // When
    tenant.suspend();

    // Then
    assertThat(tenant.isSuspended()).isTrue();
    assertThat(tenant.isActive()).isFalse();
    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.SUSPENDED);
  }

  @Test
  @DisplayName("should throw when suspending an already suspended tenant")
  void shouldThrowWhenSuspendingAlreadySuspended() {
    // Given
    Tenant tenant = validTenant();
    tenant.suspend();

    // When / Then
    assertThatThrownBy(tenant::suspend)
        .isInstanceOf(TenantSuspendedException.class)
        .hasMessageContaining("suspended");
  }

  @Test
  @DisplayName("should reactivate a suspended tenant")
  void shouldActivateSuspendedTenant() {
    // Given
    Tenant tenant = validTenant();
    tenant.suspend();

    // When
    tenant.activate();

    // Then
    assertThat(tenant.isActive()).isTrue();
    assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
  }

  @Test
  @DisplayName("should reject null id in builder")
  void shouldAcceptNullIdForNewObjects() {
    // Given / When
    Tenant tenant = Tenant.builder()
        .id(null)
        .slug(TenantSlug.of("abc"))
        .name("Name")
        .ownerEmail("e@e.com")
        .status(TenantStatus.ACTIVE)
        .build();

    // Then — null ID is valid for new objects (Hibernate generates UUID on persist)
    assertThat(tenant.getId()).isNull();
  }

  @Test
  @DisplayName("should reject blank name in builder")
  void shouldRejectBlankName() {
    // Given
    var builder = Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of("abc"))
        .name("  ")
        .ownerEmail("e@e.com")
        .status(TenantStatus.ACTIVE);

    // When / Then
    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name cannot be null or blank");
  }
}

