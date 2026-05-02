package io.cmartinezs.keygo.domain.membership.model;

import static org.assertj.core.api.Assertions.assertThat;

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
  }

  @Test
  void isActive_whenAssignmentExists_returnsTrue() {
    // Given
    TenantUserRole tur = buildRole();

    // When / Then
    assertThat(tur.isActive()).isTrue();
  }

  @Test
  void toString_marksAssignmentAsActive() {
    // Given
    TenantUserRole tur = buildRole();

    // Then
    assertThat(tur.toString()).contains("ACTIVE");
  }
}
