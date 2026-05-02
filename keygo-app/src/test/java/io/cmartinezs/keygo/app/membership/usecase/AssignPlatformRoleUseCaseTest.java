package io.cmartinezs.keygo.app.membership.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.membership.command.AssignPlatformRoleCommand;
import io.cmartinezs.keygo.app.membership.exception.PlatformRoleNotFoundException;
import io.cmartinezs.keygo.app.membership.port.PlatformRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.PlatformRoleId;
import io.cmartinezs.keygo.domain.membership.model.PlatformUserRole;
import io.cmartinezs.keygo.domain.membership.model.PlatformUserRoleId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssignPlatformRoleUseCaseTest {

  private static final UUID USER_ID = UUID.randomUUID();
  private static final String ROLE_CODE = "keygo_admin";

  @Mock private PlatformRoleRepositoryPort platformRoleRepositoryPort;
  @Mock private PlatformUserRoleRepositoryPort platformUserRoleRepositoryPort;

  @InjectMocks private AssignPlatformRoleUseCase useCase;

  @Test
  void execute_whenRoleNotFound_throwsPlatformRoleNotFoundException() {
    // Given
    AssignPlatformRoleCommand command = new AssignPlatformRoleCommand(USER_ID, List.of(ROLE_CODE));
    when(platformRoleRepositoryPort.existsByCode(ROLE_CODE)).thenReturn(false);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(PlatformRoleNotFoundException.class)
        .hasMessageContaining(ROLE_CODE);

    verify(platformUserRoleRepositoryPort, never()).assign(USER_ID, ROLE_CODE);
  }

  @Test
  void execute_whenAlreadyAssigned_returnsEmptyList() {
    // Given
    AssignPlatformRoleCommand command = new AssignPlatformRoleCommand(USER_ID, List.of(ROLE_CODE));

    when(platformRoleRepositoryPort.existsByCode(ROLE_CODE)).thenReturn(true);
    when(platformUserRoleRepositoryPort.hasRole(USER_ID, ROLE_CODE)).thenReturn(true);

    // When
    List<PlatformUserRole> result = useCase.execute(command);

    // Then
    assertThat(result).isEmpty();
    verify(platformUserRoleRepositoryPort, never()).assign(USER_ID, ROLE_CODE);
  }

  @Test
  void execute_whenNotAssigned_assignsRoleAndReturnsIt() {
    // Given
    AssignPlatformRoleCommand command = new AssignPlatformRoleCommand(USER_ID, List.of(ROLE_CODE));
    PlatformUserRole created = PlatformUserRole.builder()
        .id(PlatformUserRoleId.generate())
        .userId(UserId.generate())
        .platformRoleId(PlatformRoleId.generate())
        .build();

    when(platformRoleRepositoryPort.existsByCode(ROLE_CODE)).thenReturn(true);
    when(platformUserRoleRepositoryPort.hasRole(USER_ID, ROLE_CODE)).thenReturn(false);
    when(platformUserRoleRepositoryPort.assign(USER_ID, ROLE_CODE)).thenReturn(created);

    // When
    List<PlatformUserRole> result = useCase.execute(command);

    // Then
    assertThat(result).containsExactly(created);
    verify(platformUserRoleRepositoryPort).assign(USER_ID, ROLE_CODE);
  }

  @Test
  void execute_withMultipleRoles_assignsOnlyMissingOnes() {
    // Given
    String roleCode2 = "keygo_user";
    AssignPlatformRoleCommand command = new AssignPlatformRoleCommand(USER_ID, List.of(ROLE_CODE, roleCode2));
    PlatformUserRole created = PlatformUserRole.builder()
        .id(PlatformUserRoleId.generate())
        .userId(UserId.generate())
        .platformRoleId(PlatformRoleId.generate())
        .build();

    when(platformRoleRepositoryPort.existsByCode(ROLE_CODE)).thenReturn(true);
    when(platformRoleRepositoryPort.existsByCode(roleCode2)).thenReturn(true);
    when(platformUserRoleRepositoryPort.hasRole(USER_ID, ROLE_CODE)).thenReturn(true);
    when(platformUserRoleRepositoryPort.hasRole(USER_ID, roleCode2)).thenReturn(false);
    when(platformUserRoleRepositoryPort.assign(USER_ID, roleCode2)).thenReturn(created);

    // When
    List<PlatformUserRole> result = useCase.execute(command);

    // Then
    assertThat(result).containsExactly(created);
    verify(platformUserRoleRepositoryPort, never()).assign(USER_ID, ROLE_CODE);
    verify(platformUserRoleRepositoryPort).assign(USER_ID, roleCode2);
  }
}
