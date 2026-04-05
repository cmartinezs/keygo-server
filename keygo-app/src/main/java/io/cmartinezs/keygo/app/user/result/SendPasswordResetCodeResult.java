package io.cmartinezs.keygo.app.user.result;

import java.util.UUID;

/**
 * Resultado de {@link io.cmartinezs.keygo.app.user.usecase.SendPasswordResetCodeUseCase}.
 *
 * <p>Expone el identificador de la solicitud de reset ({@code requestId}) para que el controlador
 * lo incluya en el body del 401 y el frontend pueda redirigir al formulario de reset sin conocer
 * el email del usuario.
 *
 * @param requestId UUID de la fila en {@code password_reset_codes} persistida durante el upsert.
 * @author cmartinezs
 * @version 1.0
 */
public record SendPasswordResetCodeResult(UUID requestId) {}

