package io.cmartinezs.keygo.domain.auth.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthorizationCodeIdTest {

  @Test
  void generate_createsNonNullDistinctIds() {
    // Given / When
    AuthorizationCodeId first = AuthorizationCodeId.generate();
    AuthorizationCodeId second = AuthorizationCodeId.generate();

    // Then
    assertThat(first.id()).isNotNull();
    assertThat(second.id()).isNotNull();
    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void from_withExistingUuid_reusesValue() {
    // Given
    UUID uuid = UUID.randomUUID();

    // When
    AuthorizationCodeId id = AuthorizationCodeId.from(uuid);

    // Then
    assertThat(id.id()).isEqualTo(uuid);
    assertThat(id).hasToString(uuid.toString());
  }

  @Test
  void from_withNull_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> AuthorizationCodeId.from(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("cannot be null");
  }
}

