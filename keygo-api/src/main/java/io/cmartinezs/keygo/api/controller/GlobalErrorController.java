package io.cmartinezs.keygo.api.controller;

import io.cmartinezs.keygo.api.constant.ResponseCode;
import io.cmartinezs.keygo.api.dto.reponse.BaseResponse;
import io.cmartinezs.keygo.api.helper.ResponseHelper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Global error controller to handle errors before they reach normal controllers.
 * Controlador de error global para manejar errores antes de que lleguen a controllers normales.
 *
 * <p>This controller intercepts errors redirected from Spring and provides uniform error responses.
 * Este controlador intercepta errores redirigidos desde Spring y provee respuestas de error uniformes.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Slf4j
@RestController
public class GlobalErrorController {

  private static final String ERROR_PATH = "/error";

  /**
   * Handles all errors redirected by Spring.
   * Maneja todos los errores redirigidos por Spring.
   *
   * @param request the HTTP request / la solicitud HTTP
   * @return ResponseEntity with standardized error response / ResponseEntity con respuesta de error estandarizada
   */
  @RequestMapping(ERROR_PATH)
  public ResponseEntity<BaseResponse<Void>> handleError(HttpServletRequest request) {
    Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
    String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
    Exception exception = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

    log.error("Error handling request to {}: status={}, message={}",
        requestUri, statusCode, errorMessage, exception);

    HttpStatus status = statusCode != null
        ? HttpStatus.resolve(statusCode)
        : HttpStatus.INTERNAL_SERVER_ERROR;

    ResponseCode responseCode = mapStatusToResponseCode(status);

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .failure(ResponseHelper.message(responseCode))
        .build();

    return ResponseEntity.status(status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR)
        .body(response);
  }

  /**
   * Maps HTTP status to appropriate ResponseCode.
   * Mapea el estado HTTP al ResponseCode apropiado.
   *
   * @param status the HTTP status / el estado HTTP
   * @return the corresponding ResponseCode / el ResponseCode correspondiente
   */
  private ResponseCode mapStatusToResponseCode(HttpStatus status) {
    if (status == null) {
      return ResponseCode.OPERATION_FAILED;
    }

    return switch (status) {
      case UNAUTHORIZED -> ResponseCode.AUTHENTICATION_REQUIRED;
      case FORBIDDEN -> ResponseCode.INSUFFICIENT_PERMISSIONS;
      case NOT_FOUND -> ResponseCode.RESOURCE_NOT_FOUND;
      case BAD_REQUEST -> ResponseCode.INVALID_INPUT;
      case SERVICE_UNAVAILABLE -> ResponseCode.RESOURCE_UNAVAILABLE;
      default -> ResponseCode.OPERATION_FAILED;
    };
  }
}



