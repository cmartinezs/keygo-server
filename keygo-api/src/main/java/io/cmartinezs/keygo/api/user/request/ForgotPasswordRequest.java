package io.cmartinezs.keygo.api.user.request;

/**
 * Request body para solicitar recuperación de contraseña (self-service).
 *
 * @param email dirección de correo del usuario
 * @author cmartinezs
 * @version 1.0
 */
public record ForgotPasswordRequest(String email) {}
