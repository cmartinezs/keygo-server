package io.cmartinezs.keygo.app.membership.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListMembershipsUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID CLIENT_APP_ID = UUID.randomUUID();

  @Mock
  private MembershipRepositoryPort membershipRepositoryPort;

  @InjectMocks
  private ListMembershipsUseCase useCase;

  // ─── listByUserId ────────────────────────────────────────────────────────

  @Test
  void listByUserId_withMatchingMemberships_shouldReturnList() {
    // Given
    Membership membership = membership(USER_ID, CLIENT_APP_ID);
    when(membershipRepositoryPort.findByUserIdAndTenantSlug(USER_ID, TENANT_SLUG))
        .thenReturn(List.of(membership));

    // When
    List<Membership> result = useCase.listByUserId(USER_ID, TENANT_SLUG);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getUserId().value()).isEqualTo(USER_ID);
    verify(membershipRepositoryPort).findByUserIdAndTenantSlug(USER_ID, TENANT_SLUG);
  }

  @Test
  void listByUserId_whenNoMembershipsInTenant_shouldReturnEmptyList() {
    // Given
    when(membershipRepositoryPort.findByUserIdAndTenantSlug(USER_ID, TENANT_SLUG))
        .thenReturn(List.of());

    // When
    List<Membership> result = useCase.listByUserId(USER_ID, TENANT_SLUG);

    // Then
    assertThat(result).isEmpty();
    verify(membershipRepositoryPort).findByUserIdAndTenantSlug(USER_ID, TENANT_SLUG);
  }

  @Test
  void listByUserId_shouldNotCallUnscoped_findByUserId() {
    // Given
    when(membershipRepositoryPort.findByUserIdAndTenantSlug(USER_ID, TENANT_SLUG))
        .thenReturn(List.of());

    // When
    useCase.listByUserId(USER_ID, TENANT_SLUG);

    // Then — the unscoped method must never be called
    org.mockito.Mockito.verify(membershipRepositoryPort, org.mockito.Mockito.never())
        .findByUserId(USER_ID);
  }

  // ─── listByClientAppId ───────────────────────────────────────────────────

  @Test
  void listByClientAppId_withMatchingMemberships_shouldReturnList() {
    // Given
    Membership membership = membership(USER_ID, CLIENT_APP_ID);
    when(membershipRepositoryPort.findByClientAppIdAndTenantSlug(CLIENT_APP_ID, TENANT_SLUG))
        .thenReturn(List.of(membership));

    // When
    List<Membership> result = useCase.listByClientAppId(CLIENT_APP_ID, TENANT_SLUG);

    // Then
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getClientAppId().value()).isEqualTo(CLIENT_APP_ID);
    verify(membershipRepositoryPort).findByClientAppIdAndTenantSlug(CLIENT_APP_ID, TENANT_SLUG);
  }

  @Test
  void listByClientAppId_whenNoMembershipsInTenant_shouldReturnEmptyList() {
    // Given
    when(membershipRepositoryPort.findByClientAppIdAndTenantSlug(CLIENT_APP_ID, TENANT_SLUG))
        .thenReturn(List.of());

    // When
    List<Membership> result = useCase.listByClientAppId(CLIENT_APP_ID, TENANT_SLUG);

    // Then
    assertThat(result).isEmpty();
    verify(membershipRepositoryPort).findByClientAppIdAndTenantSlug(CLIENT_APP_ID, TENANT_SLUG);
  }

  @Test
  void listByClientAppId_shouldNotCallUnscoped_findByClientAppId() {
    // Given
    when(membershipRepositoryPort.findByClientAppIdAndTenantSlug(CLIENT_APP_ID, TENANT_SLUG))
        .thenReturn(List.of());

    // When
    useCase.listByClientAppId(CLIENT_APP_ID, TENANT_SLUG);

    // Then — the unscoped method must never be called
    org.mockito.Mockito.verify(membershipRepositoryPort, org.mockito.Mockito.never())
        .findByClientAppId(CLIENT_APP_ID);
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────

  private Membership membership(UUID userId, UUID clientAppId) {
    return Membership.builder()
        .id(MembershipId.generate())
        .userId(UserId.of(userId))
        .clientAppId(ClientAppId.of(clientAppId))
        .status(MembershipStatus.ACTIVE)
        .build();
  }
}

