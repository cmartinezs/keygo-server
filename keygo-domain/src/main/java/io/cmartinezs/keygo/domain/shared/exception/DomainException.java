package io.cmartinezs.keygo.domain.shared.exception;

/**
 * Base exception for all domain-layer errors (keygo-domain).
 * Sets {@link ExceptionLayer#DOMAIN} automatically.
 *
 * <p>Subclasses must define their own message template and accept only
 * typed values — never a free-form string built by the caller.
 */
public abstract class DomainException extends KeyGoException {

  protected DomainException(String message) {
    super(ExceptionLayer.DOMAIN, message);
  }

  protected DomainException(String message, Throwable cause) {
    super(ExceptionLayer.DOMAIN, message, cause);
  }
}
