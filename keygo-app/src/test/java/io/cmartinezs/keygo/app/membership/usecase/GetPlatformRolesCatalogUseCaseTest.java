package io.cmartinezs.keygo.app.membership.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.membership.port.PlatformRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.result.GetPlatformRolesCatalogResult;
import io.cmartinezs.keygo.domain.membership.model.PlatformRole;
import io.cmartinezs.keygo.domain.membership.model.PlatformRoleId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPlatformRolesCatalogUseCase")
class GetPlatformRolesCatalogUseCaseTest {

  @Mock private PlatformRoleRepositoryPort platformRoleRepositoryPort;

  private GetPlatformRolesCatalogUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetPlatformRolesCatalogUseCase(platformRoleRepositoryPort);
  }

  @Test
  @DisplayName("execute: returns sorted platform role catalog")
  void execute_returnsSortedCatalog() {
    PlatformRole userRole =
        PlatformRole.builder()
            .id(PlatformRoleId.of(UUID.randomUUID()))
            .code("KEYGO_USER")
            .name("Keygo User")
            .description("Global self-service access")
            .build();
    PlatformRole adminRole =
        PlatformRole.builder()
            .id(PlatformRoleId.of(UUID.randomUUID()))
            .code("KEYGO_ADMIN")
            .name("Keygo Admin")
            .description("Global platform administration")
            .build();

    when(platformRoleRepositoryPort.findAll()).thenReturn(List.of(userRole, adminRole));

    List<GetPlatformRolesCatalogResult> result = useCase.execute();

    assertThat(result).hasSize(2);
    assertThat(result.getFirst().code()).isEqualTo("KEYGO_ADMIN");
    assertThat(result.getFirst().name()).isEqualTo("Keygo Admin");
    assertThat(result.get(1).code()).isEqualTo("KEYGO_USER");
    verify(platformRoleRepositoryPort).findAll();
  }

  @Test
  @DisplayName("execute: returns empty list when catalog has no roles")
  void execute_returnsEmptyList() {
    when(platformRoleRepositoryPort.findAll()).thenReturn(List.of());

    List<GetPlatformRolesCatalogResult> result = useCase.execute();

    assertThat(result).isEmpty();
    verify(platformRoleRepositoryPort).findAll();
  }
}
