package io.cmartinezs.keygo.domain.membership.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PlatformRoleTest {

  private PlatformRole buildRole() {
    return PlatformRole.builder()
        .id(PlatformRoleId.generate())
        .code("keygo_admin")
        .name("Keygo Admin")
        .description("Test desc")
        .build();
  }

  @Test
  void builder_withValidFields_createsRole() {
    // Given / When
    PlatformRole role = buildRole();

    // Then
    assertThat(role.getId()).isNotNull();
    assertThat(role.getCode()).isEqualTo("keygo_admin");
    assertThat(role.getName()).isEqualTo("Keygo Admin");
    assertThat(role.getDescription()).isEqualTo("Test desc");
  }

  @Test
  void builder_withNullId_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> PlatformRole.builder()
        .id(null)
        .code("keygo_admin")
        .name("Keygo Admin")
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("id");
  }

  @Test
  void builder_withBlankCode_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> PlatformRole.builder()
        .id(PlatformRoleId.generate())
        .code("  ")
        .name("Keygo Admin")
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("code");
  }

  @Test
  void builder_withBlankName_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> PlatformRole.builder()
        .id(PlatformRoleId.generate())
        .code("keygo_admin")
        .name("")
        .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void updateMetadata_withValidName_updatesFields() {
    // Given
    PlatformRole role = buildRole();

    // When
    role.updateMetadata("Updated Name", "Updated desc");

    // Then
    assertThat(role.getName()).isEqualTo("Updated Name");
    assertThat(role.getDescription()).isEqualTo("Updated desc");
  }

  @Test
  void updateMetadata_withBlankName_throwsException() {
    // Given
    PlatformRole role = buildRole();

    // When / Then
    assertThatThrownBy(() -> role.updateMetadata("  ", "Some desc"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void toString_includesCode() {
    // Given
    PlatformRole role = buildRole();

    // When
    String result = role.toString();

    // Then
    assertThat(result).contains("keygo_admin");
  }
}
