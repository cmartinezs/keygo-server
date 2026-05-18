package io.cmartinezs.keygo.app.membership.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.membership.result.TenantAccessResult;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.AccessPolicy;
import io.cmartinezs.keygo.domain.clientapp.model.AllowedGrant;
import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientId;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import java.util.Set;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAccountAccessUseCaseTest {

  @Mock MembershipRepositoryPort membershipRepository;
  @Mock ClientAppRepositoryPort clientAppRepository;
  @Mock TenantRepositoryPort tenantRepository;

  @InjectMocks GetAccountAccessUseCase useCase;

  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID TENANT_UUID = UUID.randomUUID();
  private static final UUID APP_UUID = UUID.randomUUID();
  private static final UUID MEMBERSHIP_UUID = UUID.randomUUID();

  @Test
  void execute_withOneMembership_returnsTenantWithApp() {
    // Given
    Membership membership = membership(MEMBERSHIP_UUID, USER_ID, APP_UUID);
    ClientApp app = clientApp(APP_UUID, "portal-client", "Portal", TENANT_UUID);
    Tenant tenant = tenant(TENANT_UUID, "acme", "Acme Corp");

    when(membershipRepository.findByUserId(USER_ID)).thenReturn(List.of(membership));
    when(clientAppRepository.findById(ClientAppId.of(APP_UUID))).thenReturn(Optional.of(app));
    when(tenantRepository.findById(TenantId.of(TENANT_UUID))).thenReturn(Optional.of(tenant));
    when(membershipRepository.findRoleCodesByUserAndClientApp(USER_ID, APP_UUID))
        .thenReturn(List.of("ADMIN_TENANT"));

    // When
    List<TenantAccessResult> result = useCase.execute(USER_ID);

    // Then
    assertThat(result).hasSize(1);
    TenantAccessResult tenantEntry = result.get(0);
    assertThat(tenantEntry.tenantSlug()).isEqualTo("acme");
    assertThat(tenantEntry.tenantName()).isEqualTo("Acme Corp");
    assertThat(tenantEntry.apps()).hasSize(1);
    assertThat(tenantEntry.apps().get(0).appName()).isEqualTo("Portal");
    assertThat(tenantEntry.apps().get(0).roles()).containsExactly("ADMIN_TENANT");
    assertThat(tenantEntry.apps().get(0).membershipStatus()).isEqualTo("ACTIVE");
  }

  @Test
  void execute_withNoMemberships_returnsEmptyList() {
    when(membershipRepository.findByUserId(USER_ID)).thenReturn(List.of());

    List<TenantAccessResult> result = useCase.execute(USER_ID);

    assertThat(result).isEmpty();
  }

  @Test
  void execute_whenAppNotFound_skipsMembership() {
    Membership membership = membership(MEMBERSHIP_UUID, USER_ID, APP_UUID);

    when(membershipRepository.findByUserId(USER_ID)).thenReturn(List.of(membership));
    when(clientAppRepository.findById(any())).thenReturn(Optional.empty());

    List<TenantAccessResult> result = useCase.execute(USER_ID);

    assertThat(result).isEmpty();
  }

  @Test
  void execute_whenTenantNotFound_skipsMembership() {
    Membership membership = membership(MEMBERSHIP_UUID, USER_ID, APP_UUID);
    ClientApp app = clientApp(APP_UUID, "portal-client", "Portal", TENANT_UUID);

    when(membershipRepository.findByUserId(USER_ID)).thenReturn(List.of(membership));
    when(clientAppRepository.findById(ClientAppId.of(APP_UUID))).thenReturn(Optional.of(app));
    when(tenantRepository.findById(any())).thenReturn(Optional.empty());

    List<TenantAccessResult> result = useCase.execute(USER_ID);

    assertThat(result).isEmpty();
  }

  @Test
  void execute_withTwoAppsInSameTenant_groupsUnderOneTenant() {
    UUID app2Uuid = UUID.randomUUID();
    UUID membership2Uuid = UUID.randomUUID();

    Membership m1 = membership(MEMBERSHIP_UUID, USER_ID, APP_UUID);
    Membership m2 = membership(membership2Uuid, USER_ID, app2Uuid);
    ClientApp app1 = clientApp(APP_UUID, "app-one", "App One", TENANT_UUID);
    ClientApp app2 = clientApp(app2Uuid, "app-two", "App Two", TENANT_UUID);
    Tenant tenant = tenant(TENANT_UUID, "acme", "Acme Corp");

    when(membershipRepository.findByUserId(USER_ID)).thenReturn(List.of(m1, m2));
    when(clientAppRepository.findById(ClientAppId.of(APP_UUID))).thenReturn(Optional.of(app1));
    when(clientAppRepository.findById(ClientAppId.of(app2Uuid))).thenReturn(Optional.of(app2));
    when(tenantRepository.findById(TenantId.of(TENANT_UUID))).thenReturn(Optional.of(tenant));
    when(membershipRepository.findRoleCodesByUserAndClientApp(USER_ID, APP_UUID)).thenReturn(List.of());
    when(membershipRepository.findRoleCodesByUserAndClientApp(USER_ID, app2Uuid)).thenReturn(List.of());

    List<TenantAccessResult> result = useCase.execute(USER_ID);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).apps()).hasSize(2);
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────

  private static Membership membership(UUID membershipId, UUID userId, UUID clientAppId) {
    return Membership.builder()
        .id(MembershipId.of(membershipId))
        .userId(UserId.of(userId))
        .clientAppId(ClientAppId.of(clientAppId))
        .status(MembershipStatus.ACTIVE)
        .build();
  }

  private static ClientApp clientApp(UUID id, String clientId, String name, UUID tenantId) {
    return ClientApp.builder()
        .id(ClientAppId.of(id))
        .clientId(ClientId.of(clientId))
        .name(name)
        .tenantId(TenantId.of(tenantId))
        .type(ClientType.PUBLIC)
        .accessPolicy(new AccessPolicy(Set.of(AllowedGrant.AUTHORIZATION_CODE), Set.of()))
        .status(ClientAppStatus.ACTIVE)
        .build();
  }

  private static Tenant tenant(UUID id, String slug, String name) {
    return Tenant.builder()
        .id(TenantId.of(id))
        .slug(TenantSlug.of(slug))
        .name(name)
        .status(TenantStatus.ACTIVE)
        .build();
  }
}
