package io.cmartinezs.keygo.run.config;

import io.cmartinezs.keygo.app.port.out.ServiceInfoProvider;
import io.cmartinezs.keygo.app.usecase.GetServiceInfoUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ApplicationConfig
 * Pruebas unitarias para ApplicationConfig
 *
 * @author cmartinezs
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    @Mock
    private ServiceInfoProvider serviceInfoProvider;

    private final ApplicationConfig config = new ApplicationConfig();

    @Test
    void getServiceInfoUseCase_shouldReturnUseCaseInstance() {
        // When
        GetServiceInfoUseCase useCase = config.getServiceInfoUseCase(serviceInfoProvider);

        // Then
        assertThat(useCase).isNotNull();
    }

    @Test
    void getServiceInfoUseCase_shouldInjectServiceInfoProvider() {
        // When
        GetServiceInfoUseCase useCase = config.getServiceInfoUseCase(serviceInfoProvider);

        // Then
        assertThat(useCase.execute()).isEqualTo(serviceInfoProvider);
    }

    @Test
    void getServiceInfoUseCase_shouldCreateNewInstanceEachTime() {
        // When
        GetServiceInfoUseCase useCase1 = config.getServiceInfoUseCase(serviceInfoProvider);
        GetServiceInfoUseCase useCase2 = config.getServiceInfoUseCase(serviceInfoProvider);

        // Then
        assertThat(useCase1).isNotSameAs(useCase2);
    }

    @Test
    void getServiceInfoUseCase_shouldWorkWithDifferentProviders() {
        // Given
        ServiceInfoProvider provider1 = new ServiceInfoProvider() {
            @Override
            public String getTitle() { return "Title 1"; }
            @Override
            public String getName() { return "name1"; }
            @Override
            public String getVersion() { return "1.0"; }
        };

        ServiceInfoProvider provider2 = new ServiceInfoProvider() {
            @Override
            public String getTitle() { return "Title 2"; }
            @Override
            public String getName() { return "name2"; }
            @Override
            public String getVersion() { return "2.0"; }
        };

        // When
        GetServiceInfoUseCase useCase1 = config.getServiceInfoUseCase(provider1);
        GetServiceInfoUseCase useCase2 = config.getServiceInfoUseCase(provider2);

        // Then
        assertThat(useCase1.execute()).isEqualTo(provider1);
        assertThat(useCase2.execute()).isEqualTo(provider2);
        assertThat(useCase1.execute()).isNotEqualTo(provider2);
    }
}

