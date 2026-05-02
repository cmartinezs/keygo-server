package io.cmartinezs.keygo.domain.user.model;

/**
 * Propósito de un código de verificación.
 *
 * <p>Discriminador que determina el flujo de negocio al que pertenece el código:
 * <ul>
 *   <li>{@code EMAIL_VERIFICATION} — registro de usuario (verificar email).</li>
 *   <li>{@code PASSWORD_RESET} — login bloqueado por {@code status=RESET_PASSWORD} (sistema obliga).</li>
 *   <li>{@code PASSWORD_RECOVERY} — forgot-password self-service (usuario solicita).</li>
 * </ul>
 *
 * @author cmartinezs
 * @version 1.0
 */
public enum VerificationPurpose {
  EMAIL_VERIFICATION,
  PASSWORD_RESET,
  PASSWORD_RECOVERY
}
