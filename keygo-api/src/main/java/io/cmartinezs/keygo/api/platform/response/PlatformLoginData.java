package io.cmartinezs.keygo.api.platform.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de respuesta para el login de plataforma dentro del flujo PKCE.
 *
 * <p>Contiene el authorization code que el frontend intercambiará
 * por tokens en POST /platform/oauth2/token.
 *
 * @param message mensaje descriptivo
 * @param code authorization code único
 * @param redirectUri URI de redirección para el frontend
 * @author cmartinezs
 * @version 1.0
 */
public record PlatformLoginData(
    String message,
    String code,
    @JsonProperty("redirect_uri") String redirectUri) {}
