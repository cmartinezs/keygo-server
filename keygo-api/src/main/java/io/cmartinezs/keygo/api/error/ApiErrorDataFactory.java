package io.cmartinezs.keygo.api.error;

import io.cmartinezs.keygo.api.shared.MessageTranslator;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.domain.shared.exception.KeyGoException;
import io.cmartinezs.keygo.domain.user.exception.InvalidCredentialsException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;

@Component
@RequiredArgsConstructor
public final class ApiErrorDataFactory {

  private final MessageTranslator translator;

  public ErrorData fromException(
      ResponseCode responseCode, Throwable throwable, boolean includeTechnicalDetails) {
    String detail = throwable == null ? null : throwable.getMessage();
    String exception = throwable == null ? null : throwable.getClass().getSimpleName();
    return fromDetail(responseCode, detail, exception, includeTechnicalDetails, throwable);
  }

  public ErrorData fromDetail(
      ResponseCode responseCode,
      String technicalDetail,
      String exceptionName,
      boolean includeTechnicalDetails) {
    return fromDetail(responseCode, technicalDetail, exceptionName, includeTechnicalDetails, null);
  }

  private ErrorData fromDetail(
      ResponseCode responseCode,
      String technicalDetail,
      String exceptionName,
      boolean includeTechnicalDetails,
      Throwable throwable) {
    String layer = throwable instanceof KeyGoException kge ? kge.getLayer().name() : null;
    String traceId = MDC.get("traceId");

    ErrorData.ErrorDataBuilder builder =
        ErrorData.builder()
            .traceId(traceId)
            .code(responseCode.getCode())
            .layer(layer)
            .origin(origin(responseCode))
            .clientRequestCause(clientRequestCause(responseCode, throwable))
            .clientMessage(clientMessage(responseCode));

    if (includeTechnicalDetails) {
      builder
          .detail(
              (technicalDetail == null || technicalDetail.isBlank())
                  ? responseCode.getMessage()
                  : technicalDetail)
          .exception((exceptionName == null || exceptionName.isBlank()) ? "Error" : exceptionName);
    }

    return builder.build();
  }

  /**
   * Builds an ErrorData for field-level validation failures (@Valid / @Validated). Always includes
   * the list of field errors. Technical detail and rejectedValue are suppressed in production
   * (includeTechnicalDetails = false).
   */
  public ErrorData fromValidationErrors(
      ResponseCode responseCode,
      List<FieldValidationError> fieldErrors,
      boolean includeTechnicalDetails) {

    ErrorData.ErrorDataBuilder builder =
        ErrorData.builder()
            .traceId(MDC.get("traceId"))
            .code(responseCode.getCode())
            .origin(ApiErrorOrigin.CLIENT_REQUEST)
            .clientRequestCause(ApiClientRequestCause.USER_INPUT)
            .clientMessage(clientMessage(responseCode))
            .fieldErrors(fieldErrors.isEmpty() ? null : fieldErrors);

    if (includeTechnicalDetails) {
      builder
          .detail(responseCode.getMessage())
          .exception(MethodArgumentNotValidException.class.getSimpleName());
    }

    return builder.build();
  }

  private String clientMessage(ResponseCode responseCode) {
    return getLocalizedMessage(responseCode);
  }

  /**
   * Gets localized message for a response code using current locale from LocaleContextHolder. If no
   * MessageSource is available, returns hardcoded fallback message.
   *
   * @param responseCode Response code to localize
   * @return Localized message
   */
  private String getLocalizedMessage(ResponseCode responseCode) {
    var key = "error." + responseCode.getCode();
    try {
      return translator.getMessage(key, LocaleContextHolder.getLocale());
    } catch (Exception e) {
      // If message not found, return default message for this code
      return getDefaultMessage(responseCode);
    }
  }

  /**
   * Default hardcoded message (fallback if properties file is missing). Kept for backward
   * compatibility and as last-resort fallback.
   */
  private static String getDefaultMessage(ResponseCode responseCode) {
    return switch (responseCode) {
      case AUTHENTICATION_REQUIRED -> "We couldn't validate your session. Please sign in again.";
      case INVALID_INPUT -> "Please review the data you sent and try again.";
      case RESOURCE_NOT_FOUND -> "We couldn't find the resource you requested.";
      case BUSINESS_RULE_VIOLATION -> "This operation can't be completed in the current state.";
      case DUPLICATE_RESOURCE -> "This resource already exists.";
      case INSUFFICIENT_PERMISSIONS -> "You don't have permission to perform this action.";
      case EMAIL_NOT_VERIFIED -> "You must verify your email before signing in.";
      case EMAIL_VERIFICATION_EXPIRED -> "The verification code has expired. Request a new one.";
      case EMAIL_VERIFICATION_STILL_ACTIVE ->
          "You already have an active code. Wait before requesting another.";
      case CONTRACT_NOT_FOUND,
          PROVIDER_APP_NOT_FOUND,
          PLAN_VERSION_NOT_FOUND,
          SUBSCRIPTION_NOT_FOUND ->
          "We couldn't find the requested resource.";
      case CONTRACT_INVALID_STATE -> "The contract can't be processed in its current state.";
      case SUBSCRIPTION_INVALID_STATE -> "The subscription is not active and can't be cancelled.";
      case UNSUPPORTED_PKCE_METHOD -> "The requested PKCE method is not supported.";
      case CLIENT_APP_INACTIVE -> "The client application is not active.";
      case DUPLICATE_TENANT -> "A tenant with that identifier already exists.";
      default -> "We couldn't complete the request. Try again in a few moments.";
    };
  }

  private static ApiErrorOrigin origin(ResponseCode responseCode) {
    return switch (responseCode) {
      case INVALID_INPUT,
          REQUIRED_FIELD_MISSING,
          INVALID_DATA_FORMAT,
          RESOURCE_NOT_FOUND,
          CONTRACT_NOT_FOUND,
          PROVIDER_APP_NOT_FOUND,
          PLAN_VERSION_NOT_FOUND,
          SUBSCRIPTION_NOT_FOUND,
          UNSUPPORTED_PKCE_METHOD,
          AUTHENTICATION_REQUIRED ->
          ApiErrorOrigin.CLIENT_REQUEST;

      case BUSINESS_RULE_VIOLATION,
          DUPLICATE_RESOURCE,
          DUPLICATE_TENANT,
          INSUFFICIENT_PERMISSIONS,
          EMAIL_NOT_VERIFIED,
          EMAIL_VERIFICATION_EXPIRED,
          EMAIL_VERIFICATION_STILL_ACTIVE,
          CONTRACT_INVALID_STATE,
          SUBSCRIPTION_INVALID_STATE,
          CLIENT_APP_INACTIVE ->
          ApiErrorOrigin.BUSINESS_RULE;

      default -> ApiErrorOrigin.SERVER_PROCESSING;
    };
  }

  private static ApiClientRequestCause clientRequestCause(
      ResponseCode responseCode, Throwable throwable) {
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
          RESOURCE_NOT_FOUND ->
          ApiClientRequestCause.CLIENT_TECHNICAL;
      case INVALID_INPUT -> ApiClientRequestCause.USER_INPUT;
      default -> ApiClientRequestCause.CLIENT_TECHNICAL;
    };
  }
}
