package io.cmartinezs.keygo.app.membership.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.membership.command.CreateMembershipCommand;
import io.cmartinezs.keygo.app.membership.exception.DuplicateMembershipException;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.MembershipRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.exception.InvalidRoleAssignmentException;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateMembershipUseCaseTest {

  private static final String TENANT_SLUG = "acme";
  private static final UUID USER_ID = UUID.randomUUID();
  private static final UUID CLIENT_APP_ID = UUID.randomUUID();
  private static final String ROLE_CODE = "viewer";

  @Mock
  private TenantRepositoryPort tenantRepositoryPort;

  @Mock
  private MembershipRepositoryPort membershipRepositoryPort;

  @Mock
  private AppRoleRepositoryPort appRoleRepositoryPort;

  @InjectMocks
  private CreateMembershipUseCase useCase;

  @Test
  void execute_shouldCreateMembershipWithPendingStatus() {
    // Given
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(activeTenant()));
    when(membershipRepositoryPort.existsByUserAndClientApp(USER_ID, CLIENT_APP_ID))
        .thenReturn(false);
    when(appRoleRepositoryPort.findByClientAppAndCode(CLIENT_APP_ID, RoleCode.of(ROLE_CODE)))
        .thenReturn(Optional.of(appRole()));
    when(membershipRepositoryPort.save(any(Membership.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    CreateMembershipCommand command = new CreateMembershipCommand(
        TENANT_SLUG, USER_ID, CLIENT_APP_ID, Set.of(ROLE_CODE));

    // When
    Membership result = useCase.execute(command);

    // Then
    assertThat(result.getStatus()).isEqualTo(MembershipStatus.PENDING);
    verify(membershipRepositoryPort).save(any(Membership.class));
  }

  @Test
  void execute_whenTenantNotFound_shouldThrow() {
    // Given
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.empty());

    CreateMembershipCommand command = new CreateMembershipCommand(
        TENANT_SLUG, USER_ID, CLIENT_APP_ID, Set.of(ROLE_CODE));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(TenantNotFoundException.class);

    verify(membershipRepositoryPort, never()).save(any());
  }

  @Test
  void execute_whenTenantSuspended_shouldThrow() {
    // Given
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(suspendedTenant()));

    CreateMembershipCommand command = new CreateMembershipCommand(
        TENANT_SLUG, USER_ID, CLIENT_APP_ID, Set.of(ROLE_CODE));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(TenantSuspendedException.class);

    verify(membershipRepositoryPort, never()).save(any());
  }

  @Test
  void execute_whenDuplicateMembership_shouldThrow() {
    // Given
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(activeTenant()));
    when(membershipRepositoryPort.existsByUserAndClientApp(USER_ID, CLIENT_APP_ID))
        .thenReturn(true);

    CreateMembershipCommand command = new CreateMembershipCommand(
        TENANT_SLUG, USER_ID, CLIENT_APP_ID, Set.of(ROLE_CODE));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(DuplicateMembershipException.class);

    verify(membershipRepositoryPort, never()).save(any());
  }

  @Test
  void execute_whenRoleNotFound_shouldThrow() {
    // Given
    when(tenantRepositoryPort.findBySlug(TenantSlug.of(TENANT_SLUG)))
        .thenReturn(Optional.of(activeTenant()));
    when(membershipRepositoryPort.existsByUserAndClientApp(USER_ID, CLIENT_APP_ID))
        .thenReturn(false);
    when(appRoleRepositoryPort.findByClientAppAndCode(CLIENT_APP_ID, RoleCode.of(ROLE_CODE)))
        .thenReturn(Optional.empty());

    CreateMembershipCommand command = new CreateMembershipCommand(
        TENANT_SLUG, USER_ID, CLIENT_APP_ID, Set.of(ROLE_CODE));

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(InvalidRoleAssignmentException.class);

    verify(membershipRepositoryPort, never()).save(any());
  }

  // ─── Helpers ─────────────────────────────────────────────────────────────

  private Tenant activeTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Acme Corp")
        .status(TenantStatus.ACTIVE)
        .build();
  }

  private Tenant suspendedTenant() {
    return Tenant.builder()
        .id(TenantId.generate())
        .slug(TenantSlug.of(TENANT_SLUG))
        .name("Acme Corp")
        .status(TenantStatus.SUSPENDED)
        .build();
  }

  private AppRole appRole() {
    return AppRole.builder()
        .id(AppRoleId.of(UUID.randomUUID()))
        .clientAppId(ClientAppId.of(CLIENT_APP_ID))
        .code(RoleCode.of(ROLE_CODE))
        .displayName("Viewer")
        .build();
  }
}
