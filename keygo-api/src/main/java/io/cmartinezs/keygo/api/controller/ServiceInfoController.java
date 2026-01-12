package io.cmartinezs.keygo.api.controller;

import io.cmartinezs.keygo.api.constant.ResponseCode;
import io.cmartinezs.keygo.api.dto.reponse.BaseResponse;
import io.cmartinezs.keygo.api.dto.reponse.ServiceInfoData;
import io.cmartinezs.keygo.api.helper.ResponseHelper;
import io.cmartinezs.keygo.app.port.out.ServiceInfoProvider;
import io.cmartinezs.keygo.app.usecase.GetServiceInfoUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for service information
 * Controlador REST para información del servicio
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/service")
public class ServiceInfoController {

  private final GetServiceInfoUseCase getServiceInfoUseCase;

  public ServiceInfoController(GetServiceInfoUseCase getServiceInfoUseCase) {
    this.getServiceInfoUseCase = getServiceInfoUseCase;
  }

  /**
   * Get public service information
   * Obtener información pública del servicio
   *
   * @return ResponseEntity with BaseResponse containing service information
   */
  @GetMapping("/info")
  public ResponseEntity<BaseResponse<ServiceInfoData>> getServiceInfo() {
    ServiceInfoProvider info = getServiceInfoUseCase.execute();

    ServiceInfoData data = ServiceInfoData.builder()
        .title(info.getTitle())
        .name(info.getName())
        .version(info.getVersion())
        .build();

    BaseResponse<ServiceInfoData> response = BaseResponse.<ServiceInfoData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.SERVICE_INFO_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
