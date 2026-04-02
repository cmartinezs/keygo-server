package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.clientapp.port.ClientAppRepositoryPort;
import io.cmartinezs.keygo.app.membership.command.AssignRoleParentCommand;
import io.cmartinezs.keygo.app.membership.exception.AppRoleNotFoundException;
import io.cmartinezs.keygo.app.membership.port.AppRoleHierarchyPort;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.exception.RoleHierarchyCycleException;
import io.cmartinezs.keygo.domain.membership.exception.RoleHierarchyDepthExceededException;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.Tenant;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AssignRoleParentUseCase")
class AssignRoleParentUseCaseTest {

  @Mock private TenantRepositoryPort tenantRepository;
  @Mock private ClientAppRepositoryPort clientAppRepository;
  @Mock private AppRoleRepositoryPort appRoleRepository;
  @Mock private AppRoleHierarchyPort hierarchyRepository;

  private AssignRoleParentUseCase useCase;

  private static final String TENANT_SLUG = "my-tenant";
  private static final UUID CLIENT_APP_ID = UUID.randomUUID();
  private static final UUID CHILD_ID  = UUID.randomUUID();
  private static final UUID PARENT_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    useCase = new AssignRoleParentUseCase(
        tenantRepository, clientAppRepository, appRoleRepository, hierarchyRepository);
  }

  private Tenant mockTenant() {
    Tenant tenant = mock(Tenant.class);
    when(tenantRepository.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    return tenant;
  }

  private AppRole mockRole(UUID id, String code) {
    return AppRole.builder()
        .id(AppRoleId.of(id))
        .clientAppId(ClientAppId.of(CLIENT_APP_ID))
        .code(RoleCode.of(code))
        .build();
  }

  @Test
  @DisplayName("should assign parent when no cycle and depth is within limit")
  void shouldAssignParentSuccessfully() {
    mockTenant();
    when(appRoleRepository.findByClientAppAndCode(eq(CLIENT_APP_ID), eq(RoleCode.of("editor"))))
        .thenReturn(Optional.of(mockRole(CHILD_ID, "editor")));
    when(appRoleRepository.findByClientAppAndCode(eq(CLIENT_APP_ID), eq(RoleCode.of("admin"))))
        .thenReturn(Optional.of(mockRole(PARENT_ID, "admin")));
    when(hierarchyRepository.findAncestorIds(PARENT_ID)).thenReturn(List.of());

    assertThatCode(() -> useCase.execute(
        new AssignRoleParentCommand(TENANT_SLUG, CLIENT_APP_ID, "editor", "admin")))
        .doesNotThrowAnyException();

    verify(hierarchyRepository).assignParent(CHILD_ID, PARENT_ID);
  }

  @Test
  @DisplayName("should throw TenantNotFoundException when tenant does not exist")
  void shouldThrowWhenTenantNotFound() {
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(
        new AssignRoleParentCommand(TENANT_SLUG, CLIENT_APP_ID, "editor", "admin")))
        .isInstanceOf(TenantNotFoundException.class);
  }

  @Test
  @DisplayName("should throw AppRoleNotFoundException when child role does not exist")
  void shouldThrowWhenChildRoleNotFound() {
    mockTenant();
    when(appRoleRepository.findByClientAppAndCode(any(), eq(RoleCode.of("nonexistent"))))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(
        new AssignRoleParentCommand(TENANT_SLUG, CLIENT_APP_ID, "nonexistent", "admin")))
        .isInstanceOf(AppRoleNotFoundException.class);
  }

  @Test
  @DisplayName("should throw AppRoleNotFoundException when parent role does not exist")
  void shouldThrowWhenParentRoleNotFound() {
    mockTenant();
    when(appRoleRepository.findByClientAppAndCode(eq(CLIENT_APP_ID), eq(RoleCode.of("editor"))))
        .thenReturn(Optional.of(mockRole(CHILD_ID, "editor")));
    when(appRoleRepository.findByClientAppAndCode(eq(CLIENT_APP_ID), eq(RoleCode.of("nonexistent"))))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(
        new AssignRoleParentCommand(TENANT_SLUG, CLIENT_APP_ID, "editor", "nonexistent")))
        .isInstanceOf(AppRoleNotFoundException.class);
  }

  @Test
  @DisplayName("should throw RoleHierarchyCycleException on self-reference")
  void shouldThrowOnSelfReference() {
    mockTenant();
    AppRole role = mockRole(CHILD_ID, "editor");
    when(appRoleRepository.findByClientAppAndCode(any(), eq(RoleCode.of("editor"))))
        .thenReturn(Optional.of(role));

    assertThatThrownBy(() -> useCase.execute(
        new AssignRoleParentCommand(TENANT_SLUG, CLIENT_APP_ID, "editor", "editor")))
        .isInstanceOf(RoleHierarchyCycleException.class);

    verify(hierarchyRepository, never()).assignParent(any(), any());
  }

  @Test
  @DisplayName("should throw RoleHierarchyCycleException when child is already ancestor of parent")
  void shouldThrowOnCycle() {
    mockTenant();
    when(appRoleRepository.findByClientAppAndCode(eq(CLIENT_APP_ID), eq(RoleCode.of("viewer"))))
        .thenReturn(Optional.of(mockRole(CHILD_ID, "viewer")));
    when(appRoleRepository.findByClientAppAndCode(eq(CLIENT_APP_ID), eq(RoleCode.of("editor"))))
        .thenReturn(Optional.of(mockRole(PARENT_ID, "editor")));
    // parent's ancestors already contain child → cycle
    when(hierarchyRepository.findAncestorIds(PARENT_ID)).thenReturn(List.of(CHILD_ID));

    assertThatThrownBy(() -> useCase.execute(
        new AssignRoleParentCommand(TENANT_SLUG, CLIENT_APP_ID, "viewer", "editor")))
        .isInstanceOf(RoleHierarchyCycleException.class);

    verify(hierarchyRepository, never()).assignParent(any(), any());
  }

  @Test
  @DisplayName("should throw RoleHierarchyDepthExceededException when max depth would be exceeded")
  void shouldThrowOnDepthExceeded() {
    mockTenant();
    when(appRoleRepository.findByClientAppAndCode(eq(CLIENT_APP_ID), eq(RoleCode.of("editor"))))
        .thenReturn(Optional.of(mockRole(CHILD_ID, "editor")));
    when(appRoleRepository.findByClientAppAndCode(eq(CLIENT_APP_ID), eq(RoleCode.of("admin"))))
        .thenReturn(Optional.of(mockRole(PARENT_ID, "admin")));
    // parent already has MAX_DEPTH - 1 = 4 ancestors → adding one more exceeds depth 5
    List<UUID> maxAncestors = List.of(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    when(hierarchyRepository.findAncestorIds(PARENT_ID)).thenReturn(maxAncestors);

    assertThatThrownBy(() -> useCase.execute(
        new AssignRoleParentCommand(TENANT_SLUG, CLIENT_APP_ID, "editor", "admin")))
        .isInstanceOf(RoleHierarchyDepthExceededException.class);

    verify(hierarchyRepository, never()).assignParent(any(), any());
  }
}
