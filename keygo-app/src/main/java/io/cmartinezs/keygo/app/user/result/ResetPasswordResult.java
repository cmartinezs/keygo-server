package io.cmartinezs.keygo.app.user.result;

/**
 * Resultado del restablecimiento de contraseña con contraseña temporal (self-service).
 *
 * @param reset indica si la contraseña fue restablecida y la cuenta fue activada exitosamente
 * @author cmartinezs
 * @version 1.0
 */
public record ResetPasswordResult(boolean reset) {}
