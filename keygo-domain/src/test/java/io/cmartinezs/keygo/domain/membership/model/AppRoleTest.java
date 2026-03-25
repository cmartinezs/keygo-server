package io.cmartinezs.keygo.domain.membership.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AppRoleTest {

  private AppRole buildRole() {
    return AppRole.builder()
        .id(AppRoleId.generate())
        .clientAppId(new ClientAppId(UUID.randomUUID()))
        .code(RoleCode.adminRole())
        .displayName("Admin")
        .description("Administrator role")
        .build();
  }

  @Test
  void builder_withValidData_createsRole() {
    // Given / When
    AppRole role = buildRole();

    // Then
    assertThat(role.getId()).isNotNull();
    assertThat(role.getCode().value()).isEqualTo("admin");
  }

  @Test
  void builder_withNullId_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () ->
                AppRole.builder()
                    .id(null)
                    .clientAppId(new ClientAppId(UUID.randomUUID()))
                    .code(RoleCode.viewerRole())
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("id");
  }

  @Test
  void builder_withNullClientAppId_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () ->
                AppRole.builder()
                    .id(AppRoleId.generate())
                    .clientAppId(null)
                    .code(RoleCode.viewerRole())
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("clientAppId");
  }

  @Test
  void builder_withNullCode_throwsException() {
    // Given / When / Then
    assertThatThrownBy(
            () ->
                AppRole.builder()
                    .id(AppRoleId.generate())
                    .clientAppId(new ClientAppId(UUID.randomUUID()))
                    .code(null)
                    .build())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("code");
  }

  @Test
  void updateMetadata_updatesDisplayAndDescription() {
    // Given
    AppRole role = buildRole();

    // When
    role.updateMetadata("Editor", "Can edit content");

    // Then
    assertThat(role.getDisplayName()).isEqualTo("Editor");
    assertThat(role.getDescription()).isEqualTo("Can edit content");
  }
}

