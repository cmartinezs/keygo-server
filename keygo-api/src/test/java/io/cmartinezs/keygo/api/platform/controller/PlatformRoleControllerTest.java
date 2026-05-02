package io.cmartinezs.keygo.api.platform.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.api.platform.response.PlatformRoleData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.membership.result.GetPlatformRolesCatalogResult;
import io.cmartinezs.keygo.app.membership.usecase.GetPlatformRolesCatalogUseCase;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlatformRoleController")
class PlatformRoleControllerTest {

  @Mock private GetPlatformRolesCatalogUseCase getPlatformRolesCatalogUseCase;

  private PlatformRoleController controller;

  @BeforeEach
  void setUp() {
    controller = new PlatformRoleController(getPlatformRolesCatalogUseCase);
  }

  @Test
  @DisplayName("GET /platform/roles should return 200 with role catalog")
  void shouldListPlatformRolesAndReturn200() {
    when(getPlatformRolesCatalogUseCase.execute())
        .thenReturn(
            List.of(
                new GetPlatformRolesCatalogResult(
                    UUID.fromString("10000000-0000-0000-0000-000000000001"),
                    "KEYGO_ADMIN",
                    "Keygo Admin",
                    "Global platform administration"),
                new GetPlatformRolesCatalogResult(
                    UUID.fromString("10000000-0000-0000-0000-000000000002"),
                    "KEYGO_ACCOUNT_ADMIN",
                    "Keygo Account Admin",
                    "Contractor or tenant scoped account administration")));

    ResponseEntity<BaseResponse<List<PlatformRoleData>>> response = controller.getPlatformRoles();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_ROLE_LIST_RETRIEVED.getCode());
    assertThat(response.getBody().getData()).hasSize(2);
    assertThat(response.getBody().getData().getFirst().code()).isEqualTo("KEYGO_ADMIN");
    assertThat(response.getBody().getData().getFirst().name()).isEqualTo("Keygo Admin");
    verify(getPlatformRolesCatalogUseCase).execute();
  }

  @Test
  @DisplayName("GET /platform/roles should return 200 with empty list when catalog is empty")
  void shouldListPlatformRolesAndReturn200WithEmptyList() {
    when(getPlatformRolesCatalogUseCase.execute()).thenReturn(List.of());

    ResponseEntity<BaseResponse<List<PlatformRoleData>>> response = controller.getPlatformRoles();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData()).isEmpty();
    assertThat(response.getBody().getSuccess().getCode())
        .isEqualTo(ResponseCode.PLATFORM_ROLE_LIST_RETRIEVED.getCode());
    verify(getPlatformRolesCatalogUseCase).execute();
  }
}
