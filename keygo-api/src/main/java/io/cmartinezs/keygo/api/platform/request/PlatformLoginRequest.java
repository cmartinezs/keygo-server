package io.cmartinezs.keygo.api.platform.request;

/**
 * DTO de petición para login de plataforma.
 *
 * @param email    correo electrónico del usuario
 * @param password contraseña en texto plano
 */
public record PlatformLoginRequest(String email, String password) {}
