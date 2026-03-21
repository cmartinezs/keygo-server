package io.cmartinezs.keygo.domain.tenant.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for TenantId value object.
 */
@DisplayName("TenantId")
class TenantIdTest {

  @Test
  @DisplayName("should create TenantId from UUID")
  void shouldCreateFromUuid() {
    // Given
    UUID uuid = UUID.randomUUID();

    // When
    TenantId id = TenantId.of(uuid);

    // Then
    assertThat(id.value()).isEqualTo(uuid);
  }

  @Test
  @DisplayName("should create TenantId from UUID string")
  void shouldCreateFromString() {
    // Given
    UUID uuid = UUID.randomUUID();

    // When
    TenantId id = TenantId.of(uuid.toString());

    // Then
    assertThat(id.value()).isEqualTo(uuid);
  }

  @Test
  @DisplayName("should generate a unique TenantId")
  void shouldGenerate() {
    // When
    TenantId first = TenantId.generate();
    TenantId second = TenantId.generate();

    // Then
    assertThat(first).isNotEqualTo(second);
    assertThat(first.value()).isNotNull();
  }

  @Test
  @DisplayName("should reject null UUID")
  void shouldRejectNull() {
    assertThatThrownBy(() -> new TenantId(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null");
  }

  @Test
  @DisplayName("should reject null or blank string")
  void shouldRejectNullOrBlankString() {
    assertThatThrownBy(() -> TenantId.of((String) null))
        .isInstanceOf(IllegalArgumentException.class);

    assertThatThrownBy(() -> TenantId.of("  "))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

