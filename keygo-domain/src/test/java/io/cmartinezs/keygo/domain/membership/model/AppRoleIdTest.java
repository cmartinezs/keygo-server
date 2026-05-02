package io.cmartinezs.keygo.domain.membership.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AppRoleIdTest {

  @Test
  void generate_returnsRandomId() {
    // Given / When
    AppRoleId first = AppRoleId.generate();
    AppRoleId second = AppRoleId.generate();

    // Then
    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void ofUuid_withValidUuid_returnsId() {
    // Given
    UUID uuid = UUID.randomUUID();

    // When
    AppRoleId id = AppRoleId.of(uuid);

    // Then
    assertThat(id.value()).isEqualTo(uuid);
  }

  @Test
  void ofString_withInvalidUuid_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> AppRoleId.of("invalid-uuid"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid UUID format");
  }

  @Test
  void ofUuid_withNull_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> AppRoleId.of((UUID) null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null");
  }
}

