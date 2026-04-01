package io.cmartinezs.keygo.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Getter
@Builder
@RegisterReflectionForBinding
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorData {
  private final String code;
  /** Architectural layer that originated the error. Always visible to API consumers. */
  private final String layer;
  private final ApiErrorOrigin origin;
  private final ApiClientRequestCause clientRequestCause;
  private final String clientMessage;
  private final String detail;
  private final String exception;
  /** Field-level validation failures. Populated only for validation errors (400 INVALID_INPUT). */
  private final List<FieldValidationError> fieldErrors;
}

