package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;

/**
 * Se lanza cuando un código de verificación es inválido o no existe.
 *
 * <p>Unifica las anteriores {@code EmailVerificationInvalidException} y
 * {@code InvalidPasswordResetCodeException}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class VerificationCodeInvalidException extends DomainException {

  private final VerificationPurpose purpose;

  public VerificationCodeInvalidException(VerificationPurpose purpose) {
    super("Verification code is invalid or does not exist for purpose: " + purpose.name());
    this.purpose = purpose;
  }

  public VerificationCodeInvalidException(VerificationPurpose purpose, String detail) {
    super("Verification code is invalid for purpose: " + purpose.name() + " — " + detail);
    this.purpose = purpose;
  }

  public VerificationPurpose getPurpose() { return purpose; }
}
