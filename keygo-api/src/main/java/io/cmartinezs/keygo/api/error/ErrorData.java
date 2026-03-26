package io.cmartinezs.keygo.api.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

@Getter
@Builder
@RegisterReflectionForBinding
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorData {
  private final String code;
  private final String clientMessage;
  private final String detail;
  private final String exception;
}

