package io.cmartinezs.keygo.domain.clientapp.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientAppIdTest {

  @Test
  void generate_shouldReturnNonNullId() {
    // When
    ClientAppId id = ClientAppId.generate();

    // Then
    assertThat(id).isNotNull();
    assertThat(id.value()).isNotNull();
  }

  @Test
  void ofUuid_shouldWrapGivenUuid() {
    // Given
    java.util.UUID uuid = java.util.UUID.randomUUID();

    // When
    ClientAppId id = ClientAppId.of(uuid);

    // Then
    assertThat(id.value()).isEqualTo(uuid);
  }

  @Test
  void ofString_shouldParseValidUuidString() {
    // Given
    java.util.UUID uuid = java.util.UUID.randomUUID();
    String uuidStr = uuid.toString();

    // When
    ClientAppId id = ClientAppId.of(uuidStr);

    // Then
    assertThat(id.value()).isEqualTo(uuid);
  }

  @Test
  void ofString_nullValue_shouldThrow() {
    // When / Then
    assertThatThrownBy(() -> ClientAppId.of((String) null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void ofString_blankValue_shouldThrow() {
    // When / Then
    assertThatThrownBy(() -> ClientAppId.of("  "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void constructor_nullValue_shouldThrow() {
    // When / Then
    assertThatThrownBy(() -> new ClientAppId(null))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

