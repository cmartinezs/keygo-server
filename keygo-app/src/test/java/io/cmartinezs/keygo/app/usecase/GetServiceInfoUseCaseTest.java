package io.cmartinezs.keygo.app.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import io.cmartinezs.keygo.app.port.out.ServiceInfoProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for GetServiceInfoUseCase Pruebas unitarias para GetServiceInfoUseCase
 *
 * @author cmartinezs
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class GetServiceInfoUseCaseTest {

  @Mock private ServiceInfoProvider serviceInfoProvider;

  private GetServiceInfoUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetServiceInfoUseCase(serviceInfoProvider);
  }

  @Test
  void execute_shouldReturnServiceInfoProvider() {
    // Given - no stubbing needed, we just verify the provider is returned

    // When
    ServiceInfoProvider result = useCase.execute();

    // Then
    assertThat(result).isNotNull().isEqualTo(serviceInfoProvider);
  }

  @Test
  void execute_shouldReturnSameProviderInstance() {
    // When
    ServiceInfoProvider result1 = useCase.execute();
    ServiceInfoProvider result2 = useCase.execute();

    // Then
    assertThat(result1).isSameAs(result2).isSameAs(serviceInfoProvider);
  }

  @Test
  void constructor_shouldAcceptServiceInfoProvider() {
    // Given
    ServiceInfoProvider provider =
        new ServiceInfoProvider() {
          @Override
          public String getTitle() {
            return "Test Title";
          }

          @Override
          public String getName() {
            return "test-name";
          }

          @Override
          public String getVersion() {
            return "1.0.0";
          }
        };

    // When
    GetServiceInfoUseCase testUseCase = new GetServiceInfoUseCase(provider);
    ServiceInfoProvider result = testUseCase.execute();

    // Then
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("test-name");
    assertThat(result.getVersion()).isEqualTo("1.0.0");
    assertThat(result.getTitle()).isEqualTo("Test Title");
  }
}
