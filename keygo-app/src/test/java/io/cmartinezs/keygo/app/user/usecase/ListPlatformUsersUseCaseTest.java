package io.cmartinezs.keygo.app.user.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.shared.PagedResult;
import io.cmartinezs.keygo.app.user.filter.PlatformUserFilter;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListPlatformUsersUseCase")
class ListPlatformUsersUseCaseTest {

  @Mock private PlatformUserRepositoryPort platformUserRepositoryPort;

  private ListPlatformUsersUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ListPlatformUsersUseCase(platformUserRepositoryPort);
  }

  @Test
  @DisplayName("execute: returns paged platform users")
  void execute_returnsPagedPlatformUsers() {
    PlatformUser user =
        PlatformUser.builder()
            .id(UserId.of(UUID.randomUUID()))
            .username(Username.of("platform.admin"))
            .email(EmailAddress.of("platform.admin@test.com"))
            .passwordHash(PasswordHash.of("$2a$10$hashedpassword"))
            .firstName("Platform")
            .lastName("Admin")
            .status(UserStatus.ACTIVE)
            .build();
    PlatformUserFilter filter = PlatformUserFilter.of(null, null, null, 0, 20, null, null);
    PagedResult<PlatformUser> expected = PagedResult.of(List.of(user), 0, 20, 1);

    when(platformUserRepositoryPort.findAllPaged(eq(filter))).thenReturn(expected);

    PagedResult<PlatformUser> result = useCase.execute(filter);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.isLast()).isTrue();
  }

  @Test
  @DisplayName("execute: passes filter to repository unchanged")
  void execute_passesFilterToRepository() {
    PlatformUserFilter filter =
        PlatformUserFilter.of(UserStatus.ACTIVE, "platform", "test.com", 1, 10, "email", "DESC");
    when(platformUserRepositoryPort.findAllPaged(eq(filter)))
        .thenReturn(PagedResult.of(List.of(), 1, 10, 0));

    useCase.execute(filter);

    verify(platformUserRepositoryPort).findAllPaged(eq(filter));
  }
}
