package io.cmartinezs.keygo.domain.user.exception;

import io.cmartinezs.keygo.domain.shared.exception.DomainException;

/**
 * Thrown when a tenant-level login is attempted but the linked global platform user is suspended.
 * <p>Se lanza cuando un login a nivel tenant detecta que el usuario global de plataforma vinculado está suspendido.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class PlatformUserSuspendedException extends DomainException {

  public PlatformUserSuspendedException(String identifier) {
    super("Platform user account is suspended: %s".formatted(identifier));
  }
}
