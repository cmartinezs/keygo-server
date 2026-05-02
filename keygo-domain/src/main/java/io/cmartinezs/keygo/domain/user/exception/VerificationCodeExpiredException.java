package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;

/**
 * Se lanza cuando un código de verificación ha expirado, sin importar el propósito.
 *
 * <p>Unifica las anteriores {@code EmailVerificationExpiredException},
 * {@code PasswordResetCodeExpiredException} y {@code PasswordRecoveryTokenExpiredException}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class VerificationCodeExpiredException extends DomainException {

  private final VerificationPurpose purpose;

  public VerificationCodeExpiredException(VerificationPurpose purpose) {
    super("Verification code has expired for purpose: " + purpose.name());
    this.purpose = purpose;
  }

  public VerificationCodeExpiredException(VerificationPurpose purpose, String detail) {
    super("Verification code has expired for purpose: " + purpose.name() + " — " + detail);
    this.purpose = purpose;
  }

  public VerificationPurpose getPurpose() { return purpose; }
}
