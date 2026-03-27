package io.cmartinezs.keygo.app.membership.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.exception.MembershipNotFoundException;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RevokeMembershipUseCaseTest {

  private static final String TENANT_SLUG = "acme";

  @Mock
  private MembershipRepositoryPort membershipRepositoryPort;

  @InjectMocks
  private RevokeMembershipUseCase useCase;

  @Test
  void execute_whenMembershipBelongsToTenant_shouldDelete() {
    // Given
    MembershipId membershipId = MembershipId.generate();
    Membership membership = membership(membershipId);
    when(membershipRepositoryPort.findByIdAndTenantSlug(membershipId, TENANT_SLUG))
        .thenReturn(Optional.of(membership));

    // When
    useCase.execute(membershipId, TENANT_SLUG);

    // Then
    verify(membershipRepositoryPort).findByIdAndTenantSlug(membershipId, TENANT_SLUG);
    verify(membershipRepositoryPort).deleteById(membershipId);
  }

  @Test
  void execute_whenMembershipNotFound_shouldThrowMembershipNotFoundException() {
    // Given
    MembershipId membershipId = MembershipId.generate();
    when(membershipRepositoryPort.findByIdAndTenantSlug(membershipId, TENANT_SLUG))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(membershipId, TENANT_SLUG))
        .isInstanceOf(MembershipNotFoundException.class)
        .hasMessageContaining(membershipId.toString());

    verify(membershipRepositoryPort, never()).deleteById(membershipId);
  }

  @Test
  void execute_whenMembershipBelongsToDifferentTenant_shouldThrow() {
    // Given — repository returns empty because tenant slug does not match
    MembershipId membershipId = MembershipId.generate();
    when(membershipRepositoryPort.findByIdAndTenantSlug(membershipId, TENANT_SLUG))
        .thenReturn(Optional.empty());

    // When / Then
    assertThatThrownBy(() -> useCase.execute(membershipId, TENANT_SLUG))
        .isInstanceOf(MembershipNotFoundException.class);

    verify(membershipRepositoryPort, never()).deleteById(membershipId);
  }

  @Test
  void execute_shouldNeverCallUnscoped_findById() {
    // Given
    MembershipId membershipId = MembershipId.generate();
    when(membershipRepositoryPort.findByIdAndTenantSlug(membershipId, TENANT_SLUG))
        .thenReturn(Optional.of(membership(membershipId)));

    // When
    useCase.execute(membershipId, TENANT_SLUG);

    // Then — the unscoped findById must never be called
    verify(membershipRepositoryPort, never()).findById(membershipId);
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────

  private Membership membership(MembershipId membershipId) {
    return Membership.builder()
        .id(membershipId)
        .userId(UserId.of(UUID.randomUUID()))
        .clientAppId(ClientAppId.of(UUID.randomUUID()))
        .status(MembershipStatus.ACTIVE)
        .build();
  }
}

