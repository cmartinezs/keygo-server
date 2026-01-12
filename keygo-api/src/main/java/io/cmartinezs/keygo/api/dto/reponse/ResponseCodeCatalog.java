package io.cmartinezs.keygo.api.dto.reponse;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO for response code catalog
 * DTO para catálogo de códigos de respuesta
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class ResponseCodeCatalog {
  private List<ResponseCodeInfo> successCodes;
  private List<ResponseCodeInfo> failureCodes;
}

