package io.cmartinezs.keygo.app.billing.contracting.exception;

import io.cmartinezs.keygo.app.shared.exception.UseCaseException;
import java.util.UUID;

/**
 * Thrown when the provider's client application cannot be found during contract processing.
 */
public class ProviderAppNotFoundException extends UseCaseException {

  public ProviderAppNotFoundException(UUID clientAppId) {
    super("Provider client app not found by id: %s".formatted(clientAppId));
  }
}
