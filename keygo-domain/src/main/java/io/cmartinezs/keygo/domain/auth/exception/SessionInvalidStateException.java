package io.cmartinezs.keygo.domain.auth.exception;

import io.cmartinezs.keygo.domain.auth.model.SessionStatus;
import io.cmartinezs.keygo.domain.shared.exception.DomainException;

import java.util.UUID;

/**
 * Thrown when a session operation cannot proceed because the session is in an invalid state.
 */
public class SessionInvalidStateException extends DomainException {

  public SessionInvalidStateException(UUID sessionId, SessionStatus current, String operation) {
    super("Session %s cannot perform '%s' in state: %s".formatted(sessionId, operation, current));
  }
}
