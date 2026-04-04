package io.cmartinezs.keygo.app.user.command;

/**
 * Comando para solicitar el envío del código de verificación de reset de contraseña.
 *
 * <p>Utilizado cuando el login es bloqueado por {@code status=RESET_PASSWORD}:
 * el controlador llama a {@link io.cmartinezs.keygo.app.user.usecase.SendPasswordResetCodeUseCase}
 * con este comando antes de devolver 401 al cliente.
 *
 * @param tenantSlug    slug del tenant
 * @param emailOrUsername email o nombre de usuario del solicitante
 * @author cmartinezs
 * @version 1.0
 */
public record SendPasswordResetCodeCommand(String tenantSlug, String emailOrUsername) {}

