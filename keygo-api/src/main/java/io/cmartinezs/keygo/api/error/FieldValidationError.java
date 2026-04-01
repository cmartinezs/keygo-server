package io.cmartinezs.keygo.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Represents a single field-level validation failure.
 * Always includes the field name and the validation message.
 * {@code rejectedValue} is only populated in non-production environments.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldValidationError {

  /** Name of the field (or parameter) that failed validation. */
  private final String field;

  /** Human-readable validation message (e.g. "must not be blank", "must be >= 1"). */
  private final String message;

  /** The value that was rejected. Populated only in dev/local profiles. */
  private final Object rejectedValue;
}
