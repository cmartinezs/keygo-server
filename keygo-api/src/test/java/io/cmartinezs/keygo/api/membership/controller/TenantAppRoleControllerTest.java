package io.cmartinezs.keygo.api.membership.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.api.membership.request.CreateAppRoleRequest;
import io.cmartinezs.keygo.app.membership.usecase.CreateAppRoleUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListAppRolesUseCase;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class TenantAppRoleControllerTest {

  private static final String TENANT_SLUG = "keygo";
  private static final UUID CLIENT_APP_ID = UUID.randomUUID();

  @Mock private CreateAppRoleUseCase createAppRoleUseCase;
  @Mock private ListAppRolesUseCase listAppRolesUseCase;

  @InjectMocks private TenantAppRoleController controller;

  @Test
  void createAppRole_shouldReturn201WithPersistedRole() {
    // Given
    CreateAppRoleRequest request = new CreateAppRoleRequest("admin", "Administrator", "Admin role");
    AppRole persistedRole = appRole("admin");
    when(createAppRoleUseCase.execute(any())).thenReturn(persistedRole);

    // When
    var response = controller.createAppRole(TENANT_SLUG, CLIENT_APP_ID, request);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData().id()).isEqualTo(persistedRole.getId().value());
    assertThat(response.getBody().getData().code()).isEqualTo("admin");
    verify(createAppRoleUseCase).execute(any());
  }

  @Test
  void listAppRoles_shouldReturn200WithRoleList() {
    // Given
    when(listAppRolesUseCase.execute(CLIENT_APP_ID)).thenReturn(List.of(appRole("admin"), appRole("user")));

    // When
    var response = controller.listAppRoles(TENANT_SLUG, CLIENT_APP_ID);

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData()).hasSize(2);
  }

  private AppRole appRole(String code) {
    return AppRole.builder()
        .id(AppRoleId.generate())
        .clientAppId(ClientAppId.of(CLIENT_APP_ID))
        .code(RoleCode.of(code))
        .displayName(code)
        .description(code + " role")
        .build();
  }
}

