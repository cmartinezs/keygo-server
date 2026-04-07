package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;
import io.cmartinezs.keygo.domain.user.model.VerificationPurpose;

/**
 * Se lanza cuando se intenta usar un código de verificación que ya fue consumido.
 *
 * <p>Unifica la anterior {@code PasswordRecoveryTokenAlreadyUsedException}.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class VerificationCodeAlreadyUsedException extends DomainException {

  private final VerificationPurpose purpose;

  public VerificationCodeAlreadyUsedException(VerificationPurpose purpose) {
    super("Verification code has already been used for purpose: " + purpose.name());
    this.purpose = purpose;
  }

  public VerificationPurpose getPurpose() { return purpose; }
}
