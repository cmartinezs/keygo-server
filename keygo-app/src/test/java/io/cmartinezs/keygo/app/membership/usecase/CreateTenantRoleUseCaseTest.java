package io.cmartinezs.keygo.app.membership.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.membership.command.CreateTenantRoleCommand;
import io.cmartinezs.keygo.app.membership.exception.DuplicateTenantRoleException;
import io.cmartinezs.keygo.app.membership.port.TenantRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.TenantRole;
import io.cmartinezs.keygo.domain.membership.model.TenantRoleId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateTenantRoleUseCaseTest {

  private static final UUID TENANT_ID = UUID.randomUUID();
  private static final String ROLE_CODE = "TENANT_ADMIN";

  @Mock private TenantRoleRepositoryPort tenantRoleRepositoryPort;

  @InjectMocks private CreateTenantRoleUseCase useCase;

  @Test
  void execute_whenDuplicate_throwsDuplicateTenantRoleException() {
    // Given
    CreateTenantRoleCommand command = new CreateTenantRoleCommand(TENANT_ID, ROLE_CODE, "Tenant Admin", "desc");
    when(tenantRoleRepositoryPort.existsByTenantAndCode(TENANT_ID, ROLE_CODE)).thenReturn(true);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(DuplicateTenantRoleException.class)
        .hasMessageContaining(ROLE_CODE);

    verify(tenantRoleRepositoryPort, never()).create(any());
  }

  @Test
  void execute_withValidCommand_createsTenantRole() {
    // Given
    CreateTenantRoleCommand command = new CreateTenantRoleCommand(TENANT_ID, ROLE_CODE, "Tenant Admin", "desc");
    TenantRole mocked = TenantRole.builder()
        .id(TenantRoleId.generate())
        .tenantId(new TenantId(TENANT_ID))
        .code(ROLE_CODE)
        .name("Tenant Admin")
        .description("desc")
        .active(true)
        .build();

    when(tenantRoleRepositoryPort.existsByTenantAndCode(TENANT_ID, ROLE_CODE)).thenReturn(false);
    when(tenantRoleRepositoryPort.create(any())).thenReturn(mocked);

    // When
    TenantRole result = useCase.execute(command);

    // Then
    assertThat(result.getCode()).isEqualTo(ROLE_CODE);
    assertThat(result.getName()).isEqualTo("Tenant Admin");
    assertThat(result.isActive()).isTrue();
    verify(tenantRoleRepositoryPort).create(any());
  }
}
