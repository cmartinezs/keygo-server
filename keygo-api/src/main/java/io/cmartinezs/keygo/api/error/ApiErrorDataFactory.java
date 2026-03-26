package io.cmartinezs.keygo.api.error;

import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

public final class ApiErrorDataFactory {

  private ApiErrorDataFactory() {
  }

  public static ErrorData fromException(
      ResponseCode responseCode,
      Throwable throwable,
      boolean includeTechnicalDetails) {
    String detail = throwable == null ? null : throwable.getMessage();
    String exception = throwable == null ? null : throwable.getClass().getSimpleName();
    return fromDetail(responseCode, detail, exception, includeTechnicalDetails, throwable);
  }

  public static ErrorData fromDetail(
      ResponseCode responseCode,
      String technicalDetail,
      String exceptionName,
      boolean includeTechnicalDetails) {
    return fromDetail(responseCode, technicalDetail, exceptionName, includeTechnicalDetails, null);
  }

  private static ErrorData fromDetail(
      ResponseCode responseCode,
      String technicalDetail,
      String exceptionName,
      boolean includeTechnicalDetails,
      Throwable throwable) {
    ErrorData.ErrorDataBuilder builder = ErrorData.builder()
        .code(responseCode.getCode())
        .origin(origin(responseCode))
        .clientRequestCause(clientRequestCause(responseCode, throwable))
        .clientMessage(clientMessage(responseCode));

    if (includeTechnicalDetails) {
      builder
          .detail((technicalDetail == null || technicalDetail.isBlank()) ? responseCode.getMessage() : technicalDetail)
          .exception((exceptionName == null || exceptionName.isBlank()) ? "Error" : exceptionName);
    }

    return builder.build();
  }

  private static String clientMessage(ResponseCode responseCode) {
    return switch (responseCode) {
      case AUTHENTICATION_REQUIRED -> "No pudimos validar tu sesión. Inicia sesión nuevamente.";
      case INVALID_INPUT -> "Revisa los datos enviados e intenta otra vez.";
      case RESOURCE_NOT_FOUND -> "No encontramos el recurso solicitado.";
      case BUSINESS_RULE_VIOLATION -> "No se puede completar la operación con el estado actual.";
      case DUPLICATE_RESOURCE -> "El recurso ya existe.";
      case INSUFFICIENT_PERMISSIONS -> "No tienes permisos para realizar esta acción.";
      case EMAIL_NOT_VERIFIED -> "Debes verificar tu correo antes de iniciar sesión.";
      case EMAIL_VERIFICATION_EXPIRED -> "El código de verificación expiro. Solicita uno nuevo.";
      case EMAIL_VERIFICATION_STILL_ACTIVE -> "Ya tienes un código vigente. Espera antes de solicitar otro.";
      default -> "No pudimos completar la solicitud. Intenta de nuevo en unos minutos.";
    };
  }

  private static ApiErrorOrigin origin(ResponseCode responseCode) {
    return switch (responseCode) {
      case INVALID_INPUT,
          REQUIRED_FIELD_MISSING,
          INVALID_DATA_FORMAT,
          RESOURCE_NOT_FOUND,
          AUTHENTICATION_REQUIRED -> ApiErrorOrigin.CLIENT_REQUEST;

      case BUSINESS_RULE_VIOLATION,
          DUPLICATE_RESOURCE,
          INSUFFICIENT_PERMISSIONS,
          EMAIL_NOT_VERIFIED,
          EMAIL_VERIFICATION_EXPIRED,
          EMAIL_VERIFICATION_STILL_ACTIVE -> ApiErrorOrigin.BUSINESS_RULE;

      default -> ApiErrorOrigin.SERVER_PROCESSING;
    };
  }

  private static ApiClientRequestCause clientRequestCause(
      ResponseCode responseCode,
      Throwable throwable) {
    if (origin(responseCode) != ApiErrorOrigin.CLIENT_REQUEST) {
      return null;
    }

    if (throwable instanceof InvalidCredentialsException) {
      return ApiClientRequestCause.USER_INPUT;
    }

    if (throwable instanceof MethodArgumentNotValidException) {
      return ApiClientRequestCause.USER_INPUT;
    }

    if (throwable instanceof HttpMessageNotReadableException
        || throwable instanceof MissingServletRequestParameterException) {
      return ApiClientRequestCause.CLIENT_TECHNICAL;
    }

    return switch (responseCode) {
      case AUTHENTICATION_REQUIRED,
          REQUIRED_FIELD_MISSING,
          INVALID_DATA_FORMAT,
          RESOURCE_NOT_FOUND -> ApiClientRequestCause.CLIENT_TECHNICAL;
      case INVALID_INPUT -> ApiClientRequestCause.USER_INPUT;
      default -> ApiClientRequestCause.CLIENT_TECHNICAL;
    };
  }
}

