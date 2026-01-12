package io.cmartinezs.keygo.api.controller;

import io.cmartinezs.keygo.api.constant.ResponseCode;
import io.cmartinezs.keygo.api.dto.reponse.BaseResponse;
import io.cmartinezs.keygo.api.dto.reponse.ResponseCodeCatalog;
import io.cmartinezs.keygo.api.dto.reponse.ResponseCodeInfo;
import io.cmartinezs.keygo.api.helper.ResponseHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * REST controller for response code catalog
 * Controlador REST para catálogo de códigos de respuesta
 *
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/response-codes")
public class ResponseCodeController {

  /**
   * Get catalog of all response codes
   * Obtener catálogo de todos los códigos de respuesta
   *
   * @return ResponseEntity with BaseResponse containing response code catalog
   */
  @GetMapping
  public ResponseEntity<BaseResponse<ResponseCodeCatalog>> getResponseCodeCatalog() {
    List<ResponseCodeInfo> successCodes = Arrays.stream(ResponseCode.values())
        .filter(this::isSuccessCode)
        .map(this::toResponseCodeInfo)
        .toList();

    List<ResponseCodeInfo> failureCodes = Arrays.stream(ResponseCode.values())
        .filter(code -> !isSuccessCode(code))
        .map(this::toResponseCodeInfo)
        .toList();

    ResponseCodeCatalog catalog = ResponseCodeCatalog.builder()
        .successCodes(successCodes)
        .failureCodes(failureCodes)
        .build();

    BaseResponse<ResponseCodeCatalog> response = BaseResponse.<ResponseCodeCatalog>builder()
        .data(catalog)
        .success(ResponseHelper.message(ResponseCode.RESPONSE_CODES_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  private boolean isSuccessCode(ResponseCode code) {
    String codeStr = code.getCode();
    return codeStr.contains("RETRIEVED") ||
           codeStr.contains("CREATED") ||
           codeStr.contains("UPDATED") ||
           codeStr.contains("DELETED") ||
           codeStr.contains("COMPLETED");
  }

  private ResponseCodeInfo toResponseCodeInfo(ResponseCode code) {
    return ResponseCodeInfo.builder()
        .code(code.getCode())
        .message(code.getMessage())
        .type(isSuccessCode(code) ? "SUCCESS" : "FAILURE")
        .build();
  }
}

