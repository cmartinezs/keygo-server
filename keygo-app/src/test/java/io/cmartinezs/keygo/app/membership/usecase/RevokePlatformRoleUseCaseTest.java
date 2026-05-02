package io.cmartinezs.keygo.app.membership.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.membership.exception.PlatformRoleNotFoundException;
import io.cmartinezs.keygo.app.membership.port.PlatformRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RevokePlatformRoleUseCaseTest {

  private static final UUID USER_ID = UUID.randomUUID();
  private static final String ROLE_CODE = "keygo_admin";

  @Mock private PlatformRoleRepositoryPort platformRoleRepositoryPort;
  @Mock private PlatformUserRoleRepositoryPort platformUserRoleRepositoryPort;

  @InjectMocks private RevokePlatformRoleUseCase useCase;

  @Test
  void execute_whenRoleNotFound_throwsPlatformRoleNotFoundException() {
    // Given
    when(platformRoleRepositoryPort.existsByCode(ROLE_CODE)).thenReturn(false);

    // When / Then
    assertThatThrownBy(() -> useCase.execute(USER_ID, ROLE_CODE))
        .isInstanceOf(PlatformRoleNotFoundException.class)
        .hasMessageContaining(ROLE_CODE);
  }

  @Test
  void execute_whenRoleFound_revokesAssignment() {
    // Given
    when(platformRoleRepositoryPort.existsByCode(ROLE_CODE)).thenReturn(true);

    // When
    useCase.execute(USER_ID, ROLE_CODE);

    // Then
    verify(platformUserRoleRepositoryPort).revoke(USER_ID, ROLE_CODE);
  }
}
