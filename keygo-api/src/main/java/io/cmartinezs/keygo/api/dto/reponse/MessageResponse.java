package io.cmartinezs.keygo.api.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Message response for success, failure and debug messages.
 * Respuesta de mensaje para mensajes de éxito, fallo y depuración.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
  private String code;
  private String message;
}

