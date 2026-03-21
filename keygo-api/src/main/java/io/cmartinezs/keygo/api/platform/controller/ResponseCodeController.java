package io.cmartinezs.keygo.api.platform.controller;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.platform.response.ResponseCodeCatalog;
import io.cmartinezs.keygo.api.platform.response.ResponseCodeInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Platform", description = "Public platform endpoints — no authentication required")
public class ResponseCodeController {

  /**
   * Get catalog of all response codes
   * Obtener catálogo de todos los códigos de respuesta
   *
   * @return ResponseEntity with BaseResponse containing response code catalog
   */
  @GetMapping
  @Operation(
      summary = "List all response codes",
      description = "Returns the full catalog of KeyGo response codes, separated into "
                    + "success codes and failure codes. This endpoint does not require authentication.")
  @ApiResponse(
      responseCode = "200",
      description = "Response code catalog retrieved successfully",
      content = @Content(schema = @Schema(implementation = BaseResponse.class)))
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
    return codeStr.contains("RETRIEVED")
           || codeStr.contains("CREATED")
           || codeStr.contains("UPDATED")
           || codeStr.contains("DELETED")
           || codeStr.contains("COMPLETED");
  }

  private ResponseCodeInfo toResponseCodeInfo(ResponseCode code) {
    return ResponseCodeInfo.builder()
        .code(code.getCode())
        .message(code.getMessage())
        .type(isSuccessCode(code) ? "SUCCESS" : "FAILURE")
        .build();
  }
}

