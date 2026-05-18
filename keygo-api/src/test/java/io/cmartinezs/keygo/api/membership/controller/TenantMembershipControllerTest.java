package io.cmartinezs.keygo.api.membership.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.api.membership.request.CreateMembershipRequest;
import io.cmartinezs.keygo.api.membership.response.MembershipData;
import io.cmartinezs.keygo.api.membership.response.MembershipRoleData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.response.PagedData;
import io.cmartinezs.keygo.app.membership.filter.MembershipFilter;
import io.cmartinezs.keygo.app.membership.usecase.ApproveMembershipUseCase;
import io.cmartinezs.keygo.app.membership.usecase.CreateMembershipUseCase;
import io.cmartinezs.keygo.app.membership.usecase.GetMembershipRolesUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListMembershipsUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RevokeMembershipUseCase;
import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantMembershipController")
class TenantMembershipControllerTest {

  @Mock private CreateMembershipUseCase createMembershipUseCase;
  @Mock private ApproveMembershipUseCase approveMembershipUseCase;
  @Mock private ListMembershipsUseCase listMembershipsUseCase;
  @Mock private RevokeMembershipUseCase revokeMembershipUseCase;
  @Mock private GetMembershipRolesUseCase getMembershipRolesUseCase;

  private TenantMembershipController controller;

  private static final String TENANT_SLUG = "acme";
  private static final UUID MEMBERSHIP_ID = UUID.randomUUID();
  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID APP_ID = UUID.randomUUID();
  private static final UUID ROLE_ID = UUID.randomUUID();
  private static final OffsetDateTime CREATED_AT = OffsetDateTime.now().minusDays(3);

  @BeforeEach
  void setUp() {
    controller = new TenantMembershipController(
        createMembershipUseCase,
        approveMembershipUseCase,
        listMembershipsUseCase,
        revokeMembershipUseCase,
        getMembershipRolesUseCase);
  }

  private Membership membership(UUID id) {
    return Membership.builder()
        .id(MembershipId.of(id))
        .userId(UserId.of(USER_ID))
        .clientAppId(ClientAppId.of(APP_ID))
        .status(MembershipStatus.ACTIVE)
        .createdAt(CREATED_AT)
        .build();
  }

  private AppRole appRole() {
    return AppRole.builder()
        .id(AppRoleId.of(ROLE_ID))
        .clientAppId(ClientAppId.of(APP_ID))
        .code(RoleCode.of("editor"))
        .displayName("Editor")
        .build();
  }

  // ── createMembership ───────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /tenants/{tenantSlug}/memberships")
  class CreateMembershipTests {

    @Test
    @DisplayName("Debe retornar 201 con roles legibles y created_at")
    void createMembership_returns201WithRolesAndCreatedAt() {
      // Given
      Membership saved = membership(MEMBERSHIP_ID);
      when(createMembershipUseCase.execute(any())).thenReturn(saved);
      when(getMembershipRolesUseCase.execute(MEMBERSHIP_ID)).thenReturn(List.of(appRole()));

      CreateMembershipRequest request = new CreateMembershipRequest(USER_ID, APP_ID, Set.of("editor"));

      // When
      ResponseEntity<BaseResponse<MembershipData>> response =
          controller.createMembership(TENANT_SLUG, request);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      MembershipData data = response.getBody().getData();
      assertThat(data.roles()).hasSize(1);
      assertThat(data.roles().getFirst().code()).isEqualTo("editor");
      assertThat(data.roles().getFirst().displayName()).isEqualTo("Editor");
      assertThat(data.roles().getFirst().id()).isEqualTo(ROLE_ID);
      assertThat(data.createdAt()).isNotNull().isEqualTo(CREATED_AT);
      assertThat(response.getBody().getSuccess().getCode())
          .isEqualTo(ResponseCode.MEMBERSHIP_CREATED.getCode());
    }

    @Test
    @DisplayName("Debe retornar 201 con roles vacíos si no hay roles asignados")
    void createMembership_returns201WithEmptyRoles() {
      // Given
      Membership saved = membership(MEMBERSHIP_ID);
      when(createMembershipUseCase.execute(any())).thenReturn(saved);
      when(getMembershipRolesUseCase.execute(MEMBERSHIP_ID)).thenReturn(List.of());

      CreateMembershipRequest request = new CreateMembershipRequest(USER_ID, APP_ID, Set.of("viewer"));

      // When
      ResponseEntity<BaseResponse<MembershipData>> response =
          controller.createMembership(TENANT_SLUG, request);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody().getData().roles()).isEmpty();
    }
  }

  // ── listMemberships ────────────────────────────────────────────────────────

  @Nested
  @DisplayName("GET /tenants/{tenantSlug}/memberships")
  class ListMembershipsTests {

    @Test
    @DisplayName("Debe retornar 200 con lista de membresías incluyendo roles legibles y created_at")
    void listMemberships_returns200WithRolesAndCreatedAt() {
      // Given
      Membership m = membership(MEMBERSHIP_ID);
      PagedResult<Membership> paged = PagedResult.of(List.of(m), 0, 20, 1L);
      when(listMembershipsUseCase.execute(any(), any(MembershipFilter.class))).thenReturn(paged);
      when(getMembershipRolesUseCase.execute(MEMBERSHIP_ID)).thenReturn(List.of(appRole()));

      // When
      ResponseEntity<BaseResponse<PagedData<MembershipData>>> response =
          controller.listMemberships(TENANT_SLUG, null, null, null, 0, 20, null, null);

      // Then
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      List<MembershipData> content = response.getBody().getData().getContent();
      assertThat(content).hasSize(1);
      assertThat(content.getFirst().roles()).hasSize(1);
      assertThat(content.getFirst().roles().getFirst().code()).isEqualTo("editor");
      assertThat(content.getFirst().createdAt()).isEqualTo(CREATED_AT);
      assertThat(response.getBody().getSuccess().getCode())
          .isEqualTo(ResponseCode.MEMBERSHIP_LIST_RETRIEVED.getCode());
    }
  }

  // ── MembershipRoleData mapping ────────────────────────────────────────────

  @Nested
  @DisplayName("MembershipRoleData.from(AppRole)")
  class MembershipRoleDataMappingTests {

    @Test
    @DisplayName("Debe mapear id, code y display_name desde AppRole")
    void from_mapsIdCodeAndDisplayName() {
      AppRole role = appRole();
      MembershipRoleData data = MembershipRoleData.from(role);

      assertThat(data.id()).isEqualTo(ROLE_ID);
      assertThat(data.code()).isEqualTo("editor");
      assertThat(data.displayName()).isEqualTo("Editor");
    }
  }
}
