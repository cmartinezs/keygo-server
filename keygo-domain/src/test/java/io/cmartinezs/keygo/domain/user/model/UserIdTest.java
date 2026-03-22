package io.cmartinezs.keygo.domain.user.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserIdTest {

  @Test
  void generateCreatesNonNullId() {
    assertThat(UserId.generate()).isNotNull();
    assertThat(UserId.generate().value()).isNotNull();
  }

  @Test
  void ofUuidWrapsCorrectly() {
    UUID uuid = UUID.randomUUID();
    UserId id = UserId.of(uuid);
    assertThat(id.value()).isEqualTo(uuid);
  }

  @Test
  void ofStringParsesValidUuid() {
    String uuidStr = UUID.randomUUID().toString();
    UserId id = UserId.of(uuidStr);
    assertThat(id).hasToString(uuidStr);
  }

  @Test
  void ofNullUuidThrows() {
    assertThatThrownBy(() -> UserId.of((UUID) null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void ofBlankStringThrows() {
    assertThatThrownBy(() -> UserId.of("  "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void ofInvalidStringThrows() {
    assertThatThrownBy(() -> UserId.of("not-a-uuid"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}

