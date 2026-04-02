package io.cmartinezs.keygo.domain.membership.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.exception.InvalidRoleAssignmentException;
import io.cmartinezs.keygo.domain.membership.exception.MembershipAlreadySuspendedException;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MembershipTest {

  private Membership activeMembership() {
    return Membership.builder()
        .id(MembershipId.generate())
        .userId(UserId.generate())
        .clientAppId(new ClientAppId(UUID.randomUUID()))
        .status(MembershipStatus.ACTIVE)
        .build();
  }

  @Test
  void builder_withValidData_createsMembership() {
    // Given / When
    Membership membership = activeMembership();

    // Then
    assertThat(membership.getId()).isNotNull();
    assertThat(membership.isActive()).isTrue();
    assertThat(membership.isSuspended()).isFalse();
  }

  @Test
  void builder_withNullId_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () ->
                Membership.builder()
                    .id(null)
                    .userId(UserId.generate())
                    .clientAppId(new ClientAppId(UUID.randomUUID()))
                    .status(MembershipStatus.ACTIVE)
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("id");
  }

  @Test
  void builder_withNullUserId_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () ->
                Membership.builder()
                    .id(MembershipId.generate())
                    .userId(null)
                    .clientAppId(new ClientAppId(UUID.randomUUID()))
                    .status(MembershipStatus.ACTIVE)
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("userId");
  }

  @Test
  void builder_withNullClientAppId_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () ->
                Membership.builder()
                    .id(MembershipId.generate())
                    .userId(UserId.generate())
                    .clientAppId(null)
                    .status(MembershipStatus.ACTIVE)
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("clientAppId");
  }

  @Test
  void suspend_thenActivate_transitionsStatus() {
    // Given
    Membership membership = activeMembership();

    // When
    membership.suspend();
    membership.activate();

    // Then
    assertThat(membership.isActive()).isTrue();
  }

  @Test
  void suspend_whenAlreadySuspended_throwsException() {
    // Given
    Membership membership = activeMembership();
    membership.suspend();

    // When / Then
    assertThatThrownBy(membership::suspend)
        .isInstanceOf(MembershipAlreadySuspendedException.class)
        .hasMessageContaining("already suspended");
  }

  @Test
  void assignRole_withNullRole_throwsException() {
    // Given
    Membership membership = activeMembership();

    // When / Then
    assertThatThrownBy(() -> membership.assignRole(null))
        .isInstanceOf(InvalidRoleAssignmentException.class)
        .hasMessageContaining("cannot be null");
  }

  @Test
  void assignRole_withDifferentApp_throwsException() {
    // Given
    Membership membership = activeMembership();
    AppRole roleFromAnotherApp =
        AppRole.builder()
            .id(AppRoleId.generate())
            .clientAppId(new ClientAppId(UUID.randomUUID()))
            .code(RoleCode.viewerRole())
            .build();

    // When / Then
    assertThatThrownBy(() -> membership.assignRole(roleFromAnotherApp))
        .isInstanceOf(InvalidRoleAssignmentException.class)
        .hasMessageContaining("same app");
  }

  @Test
  void assignRole_andRemoveRole_updatesMembershipRoles() {
    // Given
    Membership membership = activeMembership();
    AppRole role =
        AppRole.builder()
            .id(AppRoleId.generate())
            .clientAppId(membership.getClientAppId())
            .code(RoleCode.editorRole())
            .build();

    // When
    membership.assignRole(role);

    // Then
    assertThat(membership.hasRole(role.getId())).isTrue();

    // When
    membership.removeRole(role.getId());

    // Then
    assertThat(membership.hasRole(role.getId())).isFalse();
  }
}

