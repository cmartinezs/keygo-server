package io.cmartinezs.keygo.app.user.result;

/**
 * Resultado del restablecimiento de contraseña con token de recuperación.
 *
 * @param recovered indica si la contraseña fue restablecida exitosamente
 * @author cmartinezs
 * @version 1.0
 */
public record RecoverPasswordResult(boolean recovered) {}
