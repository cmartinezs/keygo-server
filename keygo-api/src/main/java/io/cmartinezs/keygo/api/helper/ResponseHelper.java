package io.cmartinezs.keygo.api.helper;

import io.cmartinezs.keygo.api.constant.ResponseCode;
import io.cmartinezs.keygo.api.dto.reponse.MessageResponse;
import lombok.experimental.UtilityClass;

/**
 * Helper for creating standardized MessageResponse objects
 * Helper para crear objetos MessageResponse estandarizados
 *
 * @author cmartinezs
 * @version 1.0
 */
@UtilityClass
public class ResponseHelper {

  /**
   * Create MessageResponse from ResponseCode with default message
   * Crear MessageResponse desde ResponseCode con mensaje por defecto
   *
   * @param responseCode the response code enum
   * @return MessageResponse with code and default message
   */
  public static MessageResponse message(ResponseCode responseCode) {
    return MessageResponse.builder()
        .code(responseCode.getCode())
        .message(responseCode.getMessage())
        .build();
  }

  /**
   * Create MessageResponse from ResponseCode with custom message
   * Crear MessageResponse desde ResponseCode con mensaje personalizado
   *
   * @param responseCode the response code enum
   * @param customMessage custom message to override default
   * @return MessageResponse with code and custom message
   */
  public static MessageResponse message(ResponseCode responseCode, String customMessage) {
    return MessageResponse.builder()
        .code(responseCode.getCode())
        .message(customMessage)
        .build();
  }

  /**
   * Create MessageResponse with only a code (for backwards compatibility)
   * Crear MessageResponse solo con código (para compatibilidad)
   *
   * @param code the response code string
   * @param message the message
   * @return MessageResponse
   */
  public static MessageResponse message(String code, String message) {
    return MessageResponse.builder()
        .code(code)
        .message(message)
        .build();
  }
}

