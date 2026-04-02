package io.cmartinezs.keygo.domain.user.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.exception.EmailVerificationInvalidException;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EmailVerificationTest {

  @Test
  void create_withValidData_initializesUnusedVerification() {
    // Given
    UserId userId = UserId.generate();
    TenantId tenantId = TenantId.of(UUID.randomUUID());
    Instant expiresAt = Instant.now().plusSeconds(1800);

    // When
    EmailVerification verification = EmailVerification.create(userId, tenantId, "123456", expiresAt);

    // Then
    assertThat(verification.getId()).isNotNull();
    assertThat(verification.getUsedAt()).isNull();
    assertThat(verification.isUsed()).isFalse();
    assertThat(verification.isValid()).isTrue();
  }

  @Test
  void reconstitute_preservesPersistedValues() {
    // Given
    UUID id = UUID.randomUUID();
    UserId userId = UserId.generate();
    TenantId tenantId = TenantId.of(UUID.randomUUID());
    Instant expiresAt = Instant.now().plusSeconds(60);
    Instant createdAt = Instant.now().minusSeconds(10);

    // When
    EmailVerification verification =
        EmailVerification.reconstitute(id, userId, tenantId, "654321", expiresAt, null, createdAt);

    // Then
    assertThat(verification.getId()).isEqualTo(id);
    assertThat(verification.getCode()).isEqualTo("654321");
    assertThat(verification.getCreatedAt()).isEqualTo(createdAt);
  }

  @Test
  void isExpired_whenPastExpiration_returnsTrue() {
    // Given
    EmailVerification verification =
        EmailVerification.reconstitute(
            UUID.randomUUID(),
            UserId.generate(),
            TenantId.of(UUID.randomUUID()),
            "111111",
            Instant.now().minusSeconds(1),
            null,
            Instant.now().minusSeconds(300));

    // When / Then
    assertThat(verification.isExpired()).isTrue();
    assertThat(verification.isValid()).isFalse();
  }

  @Test
  void markUsed_setsUsedAt_andBlocksSecondUse() {
    // Given
    EmailVerification verification =
        EmailVerification.create(
            UserId.generate(),
            TenantId.of(UUID.randomUUID()),
            "222222",
            Instant.now().plusSeconds(300));

    // When
    verification.markUsed();

    // Then
    assertThat(verification.isUsed()).isTrue();
    assertThat(verification.getUsedAt()).isNotNull();

    // When / Then
    assertThatThrownBy(verification::markUsed)
        .isInstanceOf(EmailVerificationInvalidException.class)
        .hasMessageContaining("already used");
  }
}

