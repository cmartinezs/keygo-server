package io.cmartinezs.keygo.api.controller;

import io.cmartinezs.keygo.api.constant.ResponseCode;
import io.cmartinezs.keygo.api.dto.reponse.BaseResponse;
import io.cmartinezs.keygo.api.dto.reponse.ServiceInfoData;
import io.cmartinezs.keygo.app.port.out.ServiceInfoProvider;
import io.cmartinezs.keygo.app.usecase.GetServiceInfoUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ServiceInfoController
 * Pruebas unitarias para ServiceInfoController
 *
 * @author cmartinezs
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class ServiceInfoControllerTest {

    @Mock
    private GetServiceInfoUseCase getServiceInfoUseCase;

    @InjectMocks
    private ServiceInfoController controller;

    private ServiceInfoProvider mockServiceInfo;

    @BeforeEach
    void setUp() {
        mockServiceInfo = new ServiceInfoProvider() {
            @Override
            public String getTitle() {
                return "KeyGo Server API";
            }

            @Override
            public String getName() {
                return "keygo-server";
            }

            @Override
            public String getVersion() {
                return "1.0-SNAPSHOT";
            }
        };
    }

    @Test
    void getServiceInfo_shouldReturnServiceInformation() {
        // Given
        when(getServiceInfoUseCase.execute()).thenReturn(mockServiceInfo);

        // When
        ResponseEntity<BaseResponse<ServiceInfoData>> response = controller.getServiceInfo();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getName()).isEqualTo("keygo-server");
        assertThat(response.getBody().getData().getVersion()).isEqualTo("1.0-SNAPSHOT");
        assertThat(response.getBody().getData().getTitle()).isEqualTo("KeyGo Server API");
    }

    @Test
    void getServiceInfo_shouldReturnSuccessMessage() {
        // Given
        when(getServiceInfoUseCase.execute()).thenReturn(mockServiceInfo);

        // When
        ResponseEntity<BaseResponse<ServiceInfoData>> response = controller.getServiceInfo();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSuccess()).isNotNull();
        assertThat(response.getBody().getSuccess().getCode())
                .isEqualTo(ResponseCode.SERVICE_INFO_RETRIEVED.getCode());
    }

    @Test
    void getServiceInfo_shouldNotReturnFailureMessage() {
        // Given
        when(getServiceInfoUseCase.execute()).thenReturn(mockServiceInfo);

        // When
        ResponseEntity<BaseResponse<ServiceInfoData>> response = controller.getServiceInfo();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFailure()).isNull();
    }

    @Test
    void getServiceInfo_shouldHaveTimestamp() {
        // Given
        when(getServiceInfoUseCase.execute()).thenReturn(mockServiceInfo);

        // When
        ResponseEntity<BaseResponse<ServiceInfoData>> response = controller.getServiceInfo();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDate()).isNotNull();
    }
}

