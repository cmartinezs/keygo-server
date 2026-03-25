package io.cmartinezs.keygo.domain.auth.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenIdTest {

  @Test
  void generate_createsNonNullDistinctIds() {
    // Given / When
    RefreshTokenId first = RefreshTokenId.generate();
    RefreshTokenId second = RefreshTokenId.generate();

    // Then
    assertThat(first.value()).isNotNull();
    assertThat(second.value()).isNotNull();
    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void from_withExistingUuid_reusesValue() {
    // Given
    UUID uuid = UUID.randomUUID();

    // When
    RefreshTokenId id = RefreshTokenId.from(uuid);

    // Then
    assertThat(id.value()).isEqualTo(uuid);
    assertThat(id).hasToString(uuid.toString());
  }

  @Test
  void from_withNull_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> RefreshTokenId.from(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("cannot be null");
  }
}
