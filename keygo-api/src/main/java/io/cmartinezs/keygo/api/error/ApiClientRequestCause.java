package io.cmartinezs.keygo.api.error;

/**
 * Subclasifica errores originados por el cliente para separar causas de UI técnica
 * de problemas en datos ingresados por el usuario.
 */
public enum ApiClientRequestCause {
  CLIENT_TECHNICAL,
  USER_INPUT
}

