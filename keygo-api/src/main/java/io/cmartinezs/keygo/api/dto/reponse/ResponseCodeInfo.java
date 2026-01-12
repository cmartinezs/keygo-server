package io.cmartinezs.keygo.api.dto.reponse;

import lombok.Builder;
import lombok.Getter;

/**
 * DTO for response code information in catalog
 * DTO para información de código de respuesta en catálogo
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class ResponseCodeInfo {
  private String code;
  private String message;
  private String type; // SUCCESS or FAILURE
}

