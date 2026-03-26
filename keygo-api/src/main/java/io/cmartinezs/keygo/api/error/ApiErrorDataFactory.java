package io.cmartinezs.keygo.api.error;

import io.cmartinezs.keygo.api.shared.ResponseCode;

public final class ApiErrorDataFactory {

  private ApiErrorDataFactory() {
  }

  public static ErrorData fromException(
      ResponseCode responseCode,
      Throwable throwable,
      boolean includeTechnicalDetails) {
    String detail = throwable == null ? null : throwable.getMessage();
    String exception = throwable == null ? null : throwable.getClass().getSimpleName();
    return fromDetail(responseCode, detail, exception, includeTechnicalDetails);
  }

  public static ErrorData fromDetail(
      ResponseCode responseCode,
      String technicalDetail,
      String exceptionName,
      boolean includeTechnicalDetails) {
    ErrorData.ErrorDataBuilder builder = ErrorData.builder()
        .code(responseCode.getCode())
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
}

