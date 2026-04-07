package io.cmartinezs.keygo.api.platform.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO de respuesta para el inicio del flujo de autorización de plataforma.
 *
 * @param applicationName nombre de la aplicación (e.g. "KeyGo Platform")
 * @param redirectUri URI de redirección validada
 * @author cmartinezs
 * @version 1.0
 */
public record PlatformAuthorizationData(
    @JsonProperty("application_name") String applicationName,
    @JsonProperty("redirect_uri") String redirectUri) {}
