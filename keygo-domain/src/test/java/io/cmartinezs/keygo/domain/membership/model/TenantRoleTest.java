package io.cmartinezs.keygo.domain.membership.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantRoleTest {

  private TenantRole buildRole() {
    return TenantRole.builder()
        .id(TenantRoleId.generate())
        .tenantId(new TenantId(UUID.randomUUID()))
        .code("TENANT_ADMIN")
        .name("Tenant Admin")
        .description("Tenant administrator role")
        .build();
  }

  @Test
  void builder_withValidFields_createsRole() {
    // Given / When
    TenantRole role = buildRole();

    // Then
    assertThat(role.getId()).isNotNull();
    assertThat(role.getCode()).isEqualTo("TENANT_ADMIN");
    assertThat(role.getName()).isEqualTo("Tenant Admin");
    assertThat(role.isActive()).isTrue();
  }

  @Test
  void builder_withNullTenantId_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> TenantRole.builder()
        .id(TenantRoleId.generate())
        .tenantId(null)
        .code("TENANT_ADMIN")
        .name("Tenant Admin")
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("tenantId");
  }

  @Test
  void builder_withLowercaseCode_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> TenantRole.builder()
        .id(TenantRoleId.generate())
        .tenantId(new TenantId(UUID.randomUUID()))
        .code("tenant_admin")
        .name("Tenant Admin")
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("uppercase");
  }

  @Test
  void builder_withCodeStartingWithDigit_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> TenantRole.builder()
        .id(TenantRoleId.generate())
        .tenantId(new TenantId(UUID.randomUUID()))
        .code("1ADMIN")
        .name("Tenant Admin")
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("uppercase");
  }

  @Test
  void deactivate_setsActiveFalse() {
    // Given
    TenantRole role = buildRole();
    assertThat(role.isActive()).isTrue();

    // When
    role.deactivate();

    // Then
    assertThat(role.isActive()).isFalse();
  }

  @Test
  void reactivate_setsActiveTrue() {
    // Given
    TenantRole role = buildRole();
    role.deactivate();
    assertThat(role.isActive()).isFalse();

    // When
    role.reactivate();

    // Then
    assertThat(role.isActive()).isTrue();
  }

  @Test
  void updateMetadata_withValidName_updatesFields() {
    // Given
    TenantRole role = buildRole();

    // When
    role.updateMetadata("Updated Name", "Updated desc");

    // Then
    assertThat(role.getName()).isEqualTo("Updated Name");
    assertThat(role.getDescription()).isEqualTo("Updated desc");
  }
}
