package io.cmartinezs.keygo.domain.user.model;

import io.cmartinezs.keygo.domain.user.exception.VerificationCodeAlreadyUsedException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificationCodeTest {

  private static final UserId USER_ID = UserId.generate();

  @Test
  void create_generatesCodeWithCorrectFields() {
    // When
    VerificationCode code = VerificationCode.create(
        USER_ID, VerificationPurpose.EMAIL_VERIFICATION, "123456",
        Instant.now().plus(30, ChronoUnit.MINUTES));

    // Then
    assertThat(code.getId()).isNull(); // null = new object, Hibernate generates UUID on persist
    assertThat(code.getUserId()).isEqualTo(USER_ID);
    assertThat(code.getPurpose()).isEqualTo(VerificationPurpose.EMAIL_VERIFICATION);
    assertThat(code.getCode()).isEqualTo("123456");
    assertThat(code.getUsedAt()).isNull();
    assertThat(code.isUsed()).isFalse();
    assertThat(code.isExpired()).isFalse();
    assertThat(code.isValid()).isTrue();
  }

  @Test
  void reconstitute_preservesAllFields() {
    // Given
    UUID id = UUID.randomUUID();
    Instant expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES);
    Instant usedAt = Instant.now().minus(5, ChronoUnit.MINUTES);
    Instant createdAt = Instant.now().minus(10, ChronoUnit.MINUTES);

    // When
    VerificationCode code = VerificationCode.reconstitute(
        id, USER_ID, VerificationPurpose.PASSWORD_RESET, "654321",
        expiresAt, usedAt, createdAt);

    // Then
    assertThat(code.getId()).isEqualTo(id);
    assertThat(code.getUserId()).isEqualTo(USER_ID);
    assertThat(code.getPurpose()).isEqualTo(VerificationPurpose.PASSWORD_RESET);
    assertThat(code.getCode()).isEqualTo("654321");
    assertThat(code.getExpiresAt()).isEqualTo(expiresAt);
    assertThat(code.getUsedAt()).isEqualTo(usedAt);
    assertThat(code.getCreatedAt()).isEqualTo(createdAt);
    assertThat(code.isUsed()).isTrue();
  }

  @Test
  void isExpired_returnsTrueWhenExpired() {
    // Given — expired 1 minute ago
    VerificationCode code = VerificationCode.create(
        USER_ID, VerificationPurpose.PASSWORD_RECOVERY, "abc",
        Instant.now().minus(1, ChronoUnit.MINUTES));

    // Then
    assertThat(code.isExpired()).isTrue();
    assertThat(code.isValid()).isFalse();
  }

  @Test
  void isValid_returnsFalseWhenUsed() {
    // Given
    VerificationCode code = VerificationCode.create(
        USER_ID, VerificationPurpose.EMAIL_VERIFICATION, "111111",
        Instant.now().plus(30, ChronoUnit.MINUTES));

    // When
    code.markUsed();

    // Then
    assertThat(code.isUsed()).isTrue();
    assertThat(code.isValid()).isFalse();
    assertThat(code.getUsedAt()).isNotNull();
  }

  @Test
  void markUsed_throwsWhenAlreadyUsed() {
    // Given
    VerificationCode code = VerificationCode.create(
        USER_ID, VerificationPurpose.EMAIL_VERIFICATION, "111111",
        Instant.now().plus(30, ChronoUnit.MINUTES));
    code.markUsed();

    // When / Then
    assertThatThrownBy(code::markUsed)
        .isInstanceOf(VerificationCodeAlreadyUsedException.class);
  }
}
