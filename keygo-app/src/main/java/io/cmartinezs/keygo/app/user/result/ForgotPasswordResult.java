package io.cmartinezs.keygo.app.user.result;

/**
 * Resultado de la solicitud de recuperación de contraseña.
 *
 * <p>{@code sent} es siempre {@code true} — nunca se revela si el email existe o no
 * para prevenir enumeración de usuarios.
 *
 * @param sent              siempre true; no revela si el email existe o no
 * @param notificationEmail email ofuscado al que se envió (o se intentó enviar) la notificación
 * @author cmartinezs
 * @version 1.1
 */
public record ForgotPasswordResult(boolean sent, String notificationEmail) {}
