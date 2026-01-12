package io.cmartinezs.keygo.api.constant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
/**
 * Catalog of business response codes for API endpoints
 * These codes are specific to business operations and endpoints, not HTTP status codes
 * Catálogo de códigos de respuesta de negocio para endpoints de API
 * Estos códigos son específicos de operaciones de negocio y endpoints, no códigos de estado HTTP
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@RequiredArgsConstructor
public enum ResponseCode {
  // Service / System Operations
  SERVICE_INFO_RETRIEVED("SERVICE_INFO_RETRIEVED", "Service information retrieved successfully"),
  RESPONSE_CODES_RETRIEVED("RESPONSE_CODES_RETRIEVED", "Response codes catalog retrieved successfully"),

  // Generic Operations (use when no specific code applies)
  OPERATION_COMPLETED("OPERATION_COMPLETED", "Operation completed successfully"),
  RESOURCE_CREATED("RESOURCE_CREATED", "Resource created successfully"),
  RESOURCE_UPDATED("RESOURCE_UPDATED", "Resource updated successfully"),
  RESOURCE_DELETED("RESOURCE_DELETED", "Resource deleted successfully"),
  RESOURCE_RETRIEVED("RESOURCE_RETRIEVED", "Resource retrieved successfully"),

  // Validation & Business Rule Errors
  INVALID_INPUT("INVALID_INPUT", "Invalid input data provided"),
  REQUIRED_FIELD_MISSING("REQUIRED_FIELD_MISSING", "Required field is missing"),
  INVALID_DATA_FORMAT("INVALID_DATA_FORMAT", "Data format is invalid"),
  BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION", "Business rule validation failed"),
  DUPLICATE_RESOURCE("DUPLICATE_RESOURCE", "Resource already exists"),

  // Resource Errors
  RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "Requested resource was not found"),
  RESOURCE_UNAVAILABLE("RESOURCE_UNAVAILABLE", "Resource is temporarily unavailable"),

  // Authorization & Access
  INSUFFICIENT_PERMISSIONS("INSUFFICIENT_PERMISSIONS", "Insufficient permissions for this operation"),
  AUTHENTICATION_REQUIRED("AUTHENTICATION_REQUIRED", "Authentication is required"),

  // System Errors
  OPERATION_FAILED("OPERATION_FAILED", "Operation failed to complete"),
  EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", "External service error occurred"),
  DATABASE_ERROR("DATABASE_ERROR", "Database operation failed");

  private final String code;
  private final String message;
}
