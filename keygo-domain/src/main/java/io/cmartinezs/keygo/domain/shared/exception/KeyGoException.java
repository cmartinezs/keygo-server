package io.cmartinezs.keygo.domain.shared.exception;

/**
 * Root exception for all KeyGo-specific exceptions.
 * Carries the {@link ExceptionLayer} that originated the error so that
 * {@code ErrorData.layer} can always be populated for API consumers.
 *
 * <p>Subclass via the layer-specific abstract classes:
 * {@link DomainException}, {@code UseCaseException}, {@code PortException}.
 */
public abstract class KeyGoException extends RuntimeException {

  private final ExceptionLayer layer;

  protected KeyGoException(ExceptionLayer layer, String message) {
    super(message);
    this.layer = layer;
  }

  protected KeyGoException(ExceptionLayer layer, String message, Throwable cause) {
    super(message, cause);
    this.layer = layer;
  }

  public ExceptionLayer getLayer() {
    return layer;
  }
}
