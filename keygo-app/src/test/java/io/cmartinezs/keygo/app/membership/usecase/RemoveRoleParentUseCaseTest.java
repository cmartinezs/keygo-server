package io.cmartinezs.keygo.app.membership.usecase;

import io.cmartinezs.keygo.app.membership.exception.AppRoleNotFoundException;
import io.cmartinezs.keygo.app.membership.port.AppRoleHierarchyPort;
import io.cmartinezs.keygo.app.membership.port.AppRoleRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RemoveRoleParentUseCase")
class RemoveRoleParentUseCaseTest {

  @Mock private TenantRepositoryPort tenantRepository;
  @Mock private AppRoleRepositoryPort appRoleRepository;
  @Mock private AppRoleHierarchyPort hierarchyRepository;

  private RemoveRoleParentUseCase useCase;

  private static final String TENANT_SLUG = "my-tenant";
  private static final UUID CLIENT_APP_ID = UUID.randomUUID();
  private static final UUID ROLE_ID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    useCase = new RemoveRoleParentUseCase(tenantRepository, appRoleRepository, hierarchyRepository);
  }

  private AppRole mockRole() {
    return AppRole.builder()
        .id(AppRoleId.of(ROLE_ID))
        .clientAppId(ClientAppId.of(CLIENT_APP_ID))
        .code(RoleCode.of("editor"))
        .build();
  }

  @Test
  @DisplayName("should remove parent successfully")
  void shouldRemoveParentSuccessfully() {
    Tenant tenant = mock(Tenant.class);
    when(tenantRepository.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(appRoleRepository.findByClientAppAndCode(eq(CLIENT_APP_ID), eq(RoleCode.of("editor"))))
        .thenReturn(Optional.of(mockRole()));

    assertThatCode(() -> useCase.execute(TENANT_SLUG, CLIENT_APP_ID, "editor"))
        .doesNotThrowAnyException();

    verify(hierarchyRepository).removeParent(ROLE_ID);
  }

  @Test
  @DisplayName("should throw TenantNotFoundException when tenant does not exist")
  void shouldThrowWhenTenantNotFound() {
    when(tenantRepository.findBySlug(any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, CLIENT_APP_ID, "editor"))
        .isInstanceOf(TenantNotFoundException.class);

    verify(hierarchyRepository, never()).removeParent(any());
  }

  @Test
  @DisplayName("should throw AppRoleNotFoundException when role does not exist")
  void shouldThrowWhenRoleNotFound() {
    Tenant tenant = mock(Tenant.class);
    when(tenantRepository.findBySlug(any(TenantSlug.class))).thenReturn(Optional.of(tenant));
    when(appRoleRepository.findByClientAppAndCode(any(), any())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(TENANT_SLUG, CLIENT_APP_ID, "nonexistent"))
        .isInstanceOf(AppRoleNotFoundException.class);

    verify(hierarchyRepository, never()).removeParent(any());
  }
}
