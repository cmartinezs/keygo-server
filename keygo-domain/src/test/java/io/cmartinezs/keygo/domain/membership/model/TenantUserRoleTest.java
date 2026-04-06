package io.cmartinezs.keygo.domain.membership.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantUserRoleTest {

  private TenantUserRole buildRole() {
    return TenantUserRole.builder()
        .id(TenantUserRoleId.generate())
        .tenantUserId(UUID.randomUUID())
        .tenantRoleId(TenantRoleId.generate())
        .build();
  }

  @Test
  void builder_withValidFields_createsRole() {
    // Given / When
    TenantUserRole tur = buildRole();

    // Then
    assertThat(tur.getId()).isNotNull();
    assertThat(tur.getTenantUserId()).isNotNull();
    assertThat(tur.getTenantRoleId()).isNotNull();
    assertThat(tur.getAssignedAt()).isNotNull();
    assertThat(tur.getRemovedAt()).isNull();
  }

  @Test
  void isActive_whenRemovedAtIsNull_returnsTrue() {
    // Given
    TenantUserRole tur = buildRole();

    // When / Then
    assertThat(tur.isActive()).isTrue();
  }

  @Test
  void isActive_whenRemovedAtIsSet_returnsFalse() {
    // Given
    TenantUserRole tur = TenantUserRole.builder()
        .id(TenantUserRoleId.generate())
        .tenantUserId(UUID.randomUUID())
        .tenantRoleId(TenantRoleId.generate())
        .removedAt(Instant.now())
        .build();

    // When / Then
    assertThat(tur.isActive()).isFalse();
  }

  @Test
  void revoke_setsRemovedAt() {
    // Given
    TenantUserRole tur = buildRole();
    assertThat(tur.isActive()).isTrue();

    // When
    tur.revoke();

    // Then
    assertThat(tur.isActive()).isFalse();
    assertThat(tur.getRemovedAt()).isNotNull();
  }

  @Test
  void revoke_whenAlreadyRevoked_throwsException() {
    // Given
    TenantUserRole tur = buildRole();
    tur.revoke();
    assertThat(tur.isActive()).isFalse();

    // When / Then
    assertThatThrownBy(tur::revoke)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("already revoked");
  }
}
