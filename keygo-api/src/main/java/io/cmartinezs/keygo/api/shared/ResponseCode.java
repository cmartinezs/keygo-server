package io.cmartinezs.keygo.api.shared;

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

  // Tenant Operations
  TENANT_CREATED("TENANT_CREATED", "Tenant created successfully"),
  TENANT_RETRIEVED("TENANT_RETRIEVED", "Tenant retrieved successfully"),
  TENANT_SUSPENDED("TENANT_SUSPENDED", "Tenant suspended successfully"),

  // User Operations
  USER_CREATED("USER_CREATED", "User created successfully"),
  USER_RETRIEVED("USER_RETRIEVED", "User retrieved successfully"),
  USER_LIST_RETRIEVED("USER_LIST_RETRIEVED", "User list retrieved successfully"),
  USER_UPDATED("USER_UPDATED", "User updated successfully"),
  USER_PASSWORD_RESET("USER_PASSWORD_RESET", "User password reset successfully"),
  CREDENTIALS_VALID("CREDENTIALS_VALID", "Credentials validated successfully"),

  // Client App Operations
  CLIENT_APP_CREATED("CLIENT_APP_CREATED", "Client application created successfully"),
  CLIENT_APP_RETRIEVED("CLIENT_APP_RETRIEVED", "Client application retrieved successfully"),
  CLIENT_APP_LIST_RETRIEVED("CLIENT_APP_LIST_RETRIEVED", "Client application list retrieved successfully"),
  CLIENT_APP_UPDATED("CLIENT_APP_UPDATED", "Client application updated successfully"),
  CLIENT_APP_SECRET_ROTATED("CLIENT_APP_SECRET_ROTATED", "Client application secret rotated successfully"),

  // Membership Operations
  MEMBERSHIP_CREATED("MEMBERSHIP_CREATED", "Membership created successfully"),
  MEMBERSHIP_RETRIEVED("MEMBERSHIP_RETRIEVED", "Membership retrieved successfully"),
  MEMBERSHIP_LIST_RETRIEVED("MEMBERSHIP_LIST_RETRIEVED", "Membership list retrieved successfully"),
  MEMBERSHIP_REVOKED("MEMBERSHIP_REVOKED", "Membership revoked successfully"),
  MEMBERSHIP_SUSPENDED("MEMBERSHIP_SUSPENDED", "Membership suspended successfully"),

  // Role Operations
  ROLE_CREATED("ROLE_CREATED", "Role created successfully"),
  ROLE_RETRIEVED("ROLE_RETRIEVED", "Role retrieved successfully"),
  ROLE_LIST_RETRIEVED("ROLE_LIST_RETRIEVED", "Role list retrieved successfully"),
  ROLE_UPDATED("ROLE_UPDATED", "Role updated successfully"),
  ROLE_DELETED("ROLE_DELETED", "Role deleted successfully"),
  ROLE_ASSIGNED("ROLE_ASSIGNED", "Role assigned successfully"),

  // OAuth2 / Authorization Operations
  AUTHORIZATION_INITIATED("AUTHORIZATION_INITIATED", "Authorization flow initiated successfully"),
  AUTHORIZATION_CODE_ISSUED("AUTHORIZATION_CODE_ISSUED", "Authorization code issued successfully"),
  AUTHORIZATION_CODE_EXCHANGED("AUTHORIZATION_CODE_EXCHANGED", "Authorization code exchanged successfully"),
  LOGIN_SUCCESSFUL("LOGIN_SUCCESSFUL", "Login successful"),
  TOKEN_ISSUED("TOKEN_ISSUED", "Tokens issued successfully"),
  CLIENT_CREDENTIALS_TOKEN_ISSUED("CLIENT_CREDENTIALS_TOKEN_ISSUED", "Client credentials token issued successfully"),
  REFRESH_TOKEN_ROTATED("REFRESH_TOKEN_ROTATED", "Refresh token rotated successfully"),
  TOKEN_REVOKED("TOKEN_REVOKED", "Token revoked successfully"),
  USER_INFO_RETRIEVED("USER_INFO_RETRIEVED", "User information retrieved successfully"),
  JWKS_RETRIEVED("JWKS_RETRIEVED", "JWK Set retrieved successfully"),
  OIDC_CONFIGURATION_RETRIEVED("OIDC_CONFIGURATION_RETRIEVED", "OpenID Configuration retrieved successfully"),

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

