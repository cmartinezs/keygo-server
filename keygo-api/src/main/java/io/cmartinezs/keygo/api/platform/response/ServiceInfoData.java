package io.cmartinezs.keygo.api.platform.response;

import lombok.Builder;
import lombok.Getter;


/**
 * DTO for service information data.
 * DTO para datos de información del servicio.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
public class ServiceInfoData {
  private String title;
  private String name;
  private String version;
  private String environment;
  private String status;

}
