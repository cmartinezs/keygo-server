package io.cmartinezs.keygo.domain.membership.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MembershipRoleTest {

  @Test
  void of_withValidRoleId_createsMembershipRole() {
    // Given
    AppRoleId roleId = AppRoleId.generate();

    // When
    MembershipRole membershipRole = MembershipRole.of(roleId);

    // Then
    assertThat(membershipRole.roleId()).isEqualTo(roleId);
    assertThat(membershipRole.toString()).contains(roleId.toString());
  }

  @Test
  void constructor_withNullRoleId_throwsException() {
    // Given / When / Then
    assertThatThrownBy(() -> MembershipRole.of(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null");
  }
}

