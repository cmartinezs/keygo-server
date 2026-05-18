package io.cmartinezs.keygo.api.platform.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.api.membership.response.TenantAccessData;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.auth.usecase.RevokeTokenUseCase;
import io.cmartinezs.keygo.app.membership.result.AppAccessResult;
import io.cmartinezs.keygo.app.membership.result.TenantAccessResult;
import io.cmartinezs.keygo.app.membership.usecase.GetAccountAccessUseCase;
import io.cmartinezs.keygo.app.user.usecase.ForgotPlatformPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.GetPlatformUserProfileUseCase;
import io.cmartinezs.keygo.app.user.usecase.RecoverPlatformPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.ResetPlatformPasswordUseCase;
import io.cmartinezs.keygo.app.user.usecase.UpdatePlatformUserProfileUseCase;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class PlatformAccountControllerTest {

  @Mock ForgotPlatformPasswordUseCase forgotPasswordUseCase;
  @Mock RecoverPlatformPasswordUseCase recoverPasswordUseCase;
  @Mock ResetPlatformPasswordUseCase resetPasswordUseCase;
  @Mock RevokeTokenUseCase revokeTokenUseCase;
  @Mock GetPlatformUserProfileUseCase getPlatformUserProfileUseCase;
  @Mock UpdatePlatformUserProfileUseCase updatePlatformUserProfileUseCase;
  @Mock GetAccountAccessUseCase getAccountAccessUseCase;

  PlatformAccountController controller;

  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID TENANT_UUID = UUID.randomUUID();
  private static final UUID APP_UUID = UUID.randomUUID();
  private static final UUID MEMBERSHIP_UUID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    controller = new PlatformAccountController(
        forgotPasswordUseCase, recoverPasswordUseCase, resetPasswordUseCase,
        revokeTokenUseCase, getPlatformUserProfileUseCase, updatePlatformUserProfileUseCase,
        getAccountAccessUseCase);

    var auth = new UsernamePasswordAuthenticationToken(
        Map.of("sub", USER_ID.toString()), "token", List.of());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getAccess_withMemberships_returns200WithTenantList() {
    // Given
    AppAccessResult appEntry = new AppAccessResult(
        MEMBERSHIP_UUID, "ACTIVE", APP_UUID, "portal-client", "Portal", List.of("ADMIN_TENANT"));
    TenantAccessResult tenantEntry = new TenantAccessResult(
        TENANT_UUID, "acme", "Acme Corp", List.of(appEntry));

    when(getAccountAccessUseCase.execute(USER_ID)).thenReturn(List.of(tenantEntry));

    // When
    ResponseEntity<BaseResponse<List<TenantAccessData>>> response = controller.getAccess();

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    List<TenantAccessData> data = response.getBody().getData();
    assertThat(data).hasSize(1);
    assertThat(data.get(0).tenantSlug()).isEqualTo("acme");
    assertThat(data.get(0).apps()).hasSize(1);
    assertThat(data.get(0).apps().get(0).appName()).isEqualTo("Portal");
    assertThat(data.get(0).apps().get(0).roles()).containsExactly("ADMIN_TENANT");
  }

  @Test
  void getAccess_withNoMemberships_returns200WithEmptyList() {
    // Given
    when(getAccountAccessUseCase.execute(USER_ID)).thenReturn(List.of());

    // When
    ResponseEntity<BaseResponse<List<TenantAccessData>>> response = controller.getAccess();

    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getData()).isEmpty();
  }
}
