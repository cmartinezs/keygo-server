package io.cmartinezs.keygo.domain.auth.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SessionIdTest {

  @Test
  void generate_createsNonNullDistinctIds() {
    // Given / When
    SessionId first = SessionId.generate();
    SessionId second = SessionId.generate();

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
    SessionId id = SessionId.from(uuid);

    // Then
    assertThat(id.value()).isEqualTo(uuid).hasToString(uuid.toString());
  }

  @Test
  void from_withNull_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> SessionId.from(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("cannot be null");
  }
}

