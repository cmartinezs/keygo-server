package io.cmartinezs.keygo.app.user.result;

/**
 * Resultado del cambio de contraseña self-service.
 *
 * @param changed indica si la contraseña fue cambiada exitosamente
 * @author cmartinezs
 * @version 1.0
 */
public record ChangePasswordResult(boolean changed) {}
