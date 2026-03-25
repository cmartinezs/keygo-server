package io.cmartinezs.keygo.domain.membership.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RoleCodeTest {

  @Test
  void of_withValidCode_createsRoleCode() {
    // Given / When
    RoleCode roleCode = RoleCode.of("custom_role-1");

    // Then
    assertThat(roleCode.value()).isEqualTo("custom_role-1");
  }

  @Test
  void of_withBlankCode_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> RoleCode.of("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null or blank");
  }

  @Test
  void of_withInvalidPattern_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> RoleCode.of("1ADMIN"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("must start with lowercase letter");
  }

  @Test
  void standardFactories_returnExpectedCodes() {
    // Given / When / Then
    assertThat(RoleCode.adminRole().value()).isEqualTo(RoleCode.ADMIN);
    assertThat(RoleCode.editorRole().value()).isEqualTo(RoleCode.EDITOR);
    assertThat(RoleCode.viewerRole().value()).isEqualTo(RoleCode.VIEWER);
    assertThat(RoleCode.operatorRole().value()).isEqualTo(RoleCode.OPERATOR);
  }
}

