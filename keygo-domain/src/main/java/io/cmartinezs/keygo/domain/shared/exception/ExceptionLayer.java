package io.cmartinezs.keygo.domain.shared.exception;

/**
 * Identifies the architectural layer that originated an exception.
 * Exposed in {@code ErrorData.layer} so API consumers can categorize errors
 * without relying on technical details only available in dev environments.
 */
public enum ExceptionLayer {
  /** Pure business rules — keygo-domain */
  DOMAIN,
  /** Use-case orchestration — keygo-app */
  USE_CASE,
  /** Outbound port failure (repository, email, etc.) — keygo-app */
  PORT,
  /** HTTP entry layer — keygo-api */
  CONTROLLER
}
