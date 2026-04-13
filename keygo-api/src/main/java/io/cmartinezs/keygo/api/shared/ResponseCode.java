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
  PLATFORM_STATS_RETRIEVED("PLATFORM_STATS_RETRIEVED", "Platform statistics retrieved successfully"),
  PLATFORM_DASHBOARD_RETRIEVED("PLATFORM_DASHBOARD_RETRIEVED", "Platform dashboard retrieved successfully"),

  // Platform Authentication
  PLATFORM_AUTHORIZATION_INITIATED("PLATFORM_AUTHORIZATION_INITIATED", "Platform authorization initiated successfully"),
  PLATFORM_LOGIN_SUCCESS("PLATFORM_LOGIN_SUCCESS", "Platform login successful"),
  PLATFORM_USER_EMAIL_FOUND("PLATFORM_USER_EMAIL_FOUND", "Platform user email found"),
  PLATFORM_USER_EMAIL_NOT_FOUND("PLATFORM_USER_EMAIL_NOT_FOUND", "Platform user email not found"),
  PLATFORM_TOKEN_ISSUED("PLATFORM_TOKEN_ISSUED", "Platform tokens issued successfully"),
  PLATFORM_TOKEN_ROTATED("PLATFORM_TOKEN_ROTATED", "Platform token rotated successfully"),
  PLATFORM_TOKEN_REVOKED("PLATFORM_TOKEN_REVOKED", "Platform token revoked successfully"),

  // Platform User Management
  PLATFORM_USER_CREATED("PLATFORM_USER_CREATED", "Platform user created successfully"),
  PLATFORM_USER_RETRIEVED("PLATFORM_USER_RETRIEVED", "Platform user retrieved successfully"),
  PLATFORM_USER_LIST_RETRIEVED("PLATFORM_USER_LIST_RETRIEVED", "Platform user list retrieved successfully"),
  PLATFORM_USER_UPDATED("PLATFORM_USER_UPDATED", "Platform user updated successfully"),
  PLATFORM_USER_SUSPENDED("PLATFORM_USER_SUSPENDED", "Platform user suspended successfully"),
  PLATFORM_USER_ACTIVATED("PLATFORM_USER_ACTIVATED", "Platform user activated successfully"),
  PLATFORM_ROLE_ASSIGNED("PLATFORM_ROLE_ASSIGNED", "Platform role assigned successfully"),
  PLATFORM_ROLE_REVOKED("PLATFORM_ROLE_REVOKED", "Platform role revoked successfully"),

  // Tenant Operations
  TENANT_CREATED("TENANT_CREATED", "Tenant created successfully"),
  TENANT_RETRIEVED("TENANT_RETRIEVED", "Tenant retrieved successfully"),
  TENANT_LIST_RETRIEVED("TENANT_LIST_RETRIEVED", "Tenant list retrieved successfully"),
  TENANT_SUSPENDED("TENANT_SUSPENDED", "Tenant suspended successfully"),
  TENANT_ACTIVATED("TENANT_ACTIVATED", "Tenant activated successfully"),

  // Account Self-Service Operations
  ACCOUNT_PASSWORD_CHANGED("ACCOUNT_PASSWORD_CHANGED", "Account password changed successfully"),
  ACCOUNT_PASSWORD_RESET("ACCOUNT_PASSWORD_RESET", "Account password reset successfully"),
  ACCOUNT_PASSWORD_RECOVERY_SENT("ACCOUNT_PASSWORD_RECOVERY_SENT", "Password recovery email sent successfully"),
  ACCOUNT_PASSWORD_RECOVERED("ACCOUNT_PASSWORD_RECOVERED", "Account password recovered successfully"),
  ACCOUNT_SESSIONS_RETRIEVED("ACCOUNT_SESSIONS_RETRIEVED", "Account sessions retrieved successfully"),
  ACCOUNT_SESSION_REVOKED("ACCOUNT_SESSION_REVOKED", "Account session revoked successfully"),
  ACCOUNT_SESSION_ALREADY_CLOSED("ACCOUNT_SESSION_ALREADY_CLOSED", "Account session was already closed"),
  ACCOUNT_NOTIFICATION_PREFERENCES_RETRIEVED("ACCOUNT_NOTIFICATION_PREFERENCES_RETRIEVED", "Account notification preferences retrieved successfully"),
  ACCOUNT_NOTIFICATION_PREFERENCES_UPDATED("ACCOUNT_NOTIFICATION_PREFERENCES_UPDATED", "Account notification preferences updated successfully"),
  ACCOUNT_ACCESS_RETRIEVED("ACCOUNT_ACCESS_RETRIEVED", "Account access retrieved successfully"),

  // User Admin Operations
  USER_SUSPENDED("USER_SUSPENDED", "User account suspended successfully"),
  USER_ACTIVATED("USER_ACTIVATED", "User account activated successfully"),
  USER_SESSIONS_RETRIEVED("USER_SESSIONS_RETRIEVED", "User sessions retrieved successfully"),
  RESET_PASSWORD_REQUIRED("RESET_PASSWORD_REQUIRED", "User must reset their password before logging in"),
  RESET_PASSWORD_CODE_SENT("RESET_PASSWORD_CODE_SENT", "Password reset verification code sent to user email"),

  // User Operations
  USER_CREATED("USER_CREATED", "User created successfully"),
  USER_REGISTERED("USER_REGISTERED", "User registered successfully. Verification email sent."),
  EMAIL_VERIFICATION_SENT("EMAIL_VERIFICATION_SENT", "Email verification code sent successfully"),
  EMAIL_VERIFIED("EMAIL_VERIFIED", "Email verified successfully. User is now active."),
  EMAIL_VERIFICATION_EXPIRED("EMAIL_VERIFICATION_EXPIRED", "Email verification code has expired. Please request a new one."),
  EMAIL_VERIFICATION_RESENT("EMAIL_VERIFICATION_RESENT", "Email verification code resent successfully"),
  EMAIL_VERIFICATION_STILL_ACTIVE("EMAIL_VERIFICATION_STILL_ACTIVE", "Current verification code is still active. Please wait until it expires before requesting a new one."),
  EMAIL_NOT_VERIFIED("EMAIL_NOT_VERIFIED", "User email has not been verified. Please check your inbox and verify your account."),
  USER_RETRIEVED("USER_RETRIEVED", "User retrieved successfully"),
  USER_LIST_RETRIEVED("USER_LIST_RETRIEVED", "User list retrieved successfully"),
  USER_UPDATED("USER_UPDATED", "User updated successfully"),
  USER_PASSWORD_RESET("USER_PASSWORD_RESET", "User password reset successfully"),
  USER_PROFILE_RETRIEVED("USER_PROFILE_RETRIEVED", "User profile retrieved successfully"),
  USER_PROFILE_UPDATED("USER_PROFILE_UPDATED", "User profile updated successfully"),
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
  MEMBERSHIP_APPROVED("MEMBERSHIP_APPROVED", "Membership approved successfully"),

  // Role Operations
  ROLE_CREATED("ROLE_CREATED", "Role created successfully"),
  ROLE_RETRIEVED("ROLE_RETRIEVED", "Role retrieved successfully"),
  ROLE_LIST_RETRIEVED("ROLE_LIST_RETRIEVED", "Role list retrieved successfully"),
  ROLE_UPDATED("ROLE_UPDATED", "Role updated successfully"),
  ROLE_DELETED("ROLE_DELETED", "Role deleted successfully"),
  ROLE_ASSIGNED("ROLE_ASSIGNED", "Role assigned successfully"),
  ROLE_PARENT_ASSIGNED("ROLE_PARENT_ASSIGNED", "Role parent assigned successfully"),
  ROLE_PARENT_REMOVED("ROLE_PARENT_REMOVED", "Role parent removed successfully"),

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

  // Billing — Catalog
  APP_PLAN_CATALOG_RETRIEVED("APP_PLAN_CATALOG_RETRIEVED", "App plan catalog retrieved successfully"),
  APP_PLAN_RETRIEVED("APP_PLAN_RETRIEVED", "App plan retrieved successfully"),
  APP_PLAN_CREATED("APP_PLAN_CREATED", "App plan created successfully"),

  // Billing — Contracting errors
  CONTRACT_NOT_FOUND("CONTRACT_NOT_FOUND", "Contract not found"),
  CONTRACT_INVALID_STATE("CONTRACT_INVALID_STATE", "Contract is in an invalid state for this operation"),
  CONTRACTOR_EMAIL_ALREADY_EXISTS("CONTRACTOR_EMAIL_ALREADY_EXISTS", "A contractor with this email already exists"),
  PROVIDER_APP_NOT_FOUND("PROVIDER_APP_NOT_FOUND", "Provider application not found"),
  PLAN_VERSION_NOT_FOUND("PLAN_VERSION_NOT_FOUND", "Plan version not found"),

  // Billing — Subscription errors
  SUBSCRIPTION_NOT_FOUND("SUBSCRIPTION_NOT_FOUND", "Subscription not found"),
  SUBSCRIPTION_INVALID_STATE("SUBSCRIPTION_INVALID_STATE", "Subscription is not in a valid state for this operation"),

  // Auth errors
  UNSUPPORTED_PKCE_METHOD("UNSUPPORTED_PKCE_METHOD", "The requested PKCE method is not supported"),

  // Client App errors
  CLIENT_APP_INACTIVE("CLIENT_APP_INACTIVE", "Client application is not active"),

  // Tenant errors
  DUPLICATE_TENANT("DUPLICATE_TENANT", "A tenant with this slug already exists"),

  // Billing — Contracting
  APP_CONTRACT_CREATED("APP_CONTRACT_CREATED", "App contract created successfully"),
  APP_CONTRACT_RETRIEVED("APP_CONTRACT_RETRIEVED", "App contract retrieved successfully"),
  APP_CONTRACT_EMAIL_VERIFIED("APP_CONTRACT_EMAIL_VERIFIED", "Contract email verified successfully"),
  APP_CONTRACT_PAYMENT_APPROVED("APP_CONTRACT_PAYMENT_APPROVED", "Payment approved for contract"),
  APP_CONTRACT_ACTIVATED("APP_CONTRACT_ACTIVATED", "App contract activated successfully"),
  APP_CONTRACT_ONBOARDING_RESUMED("APP_CONTRACT_ONBOARDING_RESUMED", "Contract onboarding state retrieved successfully"),
  APP_CONTRACT_VERIFICATION_RESENT("APP_CONTRACT_VERIFICATION_RESENT", "Verification code resent to contractor email"),
  APP_INVALID_PLAN_SUBSCRIBER_TYPE("APP_INVALID_PLAN_SUBSCRIBER_TYPE", "Contract and plan have incompatible subscriber types"),

  // Billing — Subscription
  APP_SUBSCRIPTION_RETRIEVED("APP_SUBSCRIPTION_RETRIEVED", "App subscription retrieved successfully"),
  APP_SUBSCRIPTION_CANCELLED("APP_SUBSCRIPTION_CANCELLED", "App subscription marked for cancellation at period end"),

  // Billing — Invoices
  APP_INVOICE_LIST_RETRIEVED("APP_INVOICE_LIST_RETRIEVED", "Invoice list retrieved successfully"),
  APP_INVOICE_RETRIEVED("APP_INVOICE_RETRIEVED", "Invoice retrieved successfully"),

  // Billing — Usage
  APP_USAGE_RETRIEVED("APP_USAGE_RETRIEVED", "App usage retrieved successfully"),
  APP_LIMITS_RETRIEVED("APP_LIMITS_RETRIEVED", "App limits retrieved successfully"),

  // Billing — Platform
  PLATFORM_PLAN_CATALOG_RETRIEVED("PLATFORM_PLAN_CATALOG_RETRIEVED", "Platform plan catalog retrieved successfully"),
  PLATFORM_PLAN_RETRIEVED("PLATFORM_PLAN_RETRIEVED", "Platform plan retrieved successfully"),
  PLATFORM_SUBSCRIPTION_RETRIEVED("PLATFORM_SUBSCRIPTION_RETRIEVED", "Platform subscription retrieved successfully"),
  PLATFORM_SUBSCRIPTION_CANCELLED("PLATFORM_SUBSCRIPTION_CANCELLED", "Platform subscription marked for cancellation"),
  PLATFORM_INVOICES_RETRIEVED("PLATFORM_INVOICES_RETRIEVED", "Platform invoices retrieved successfully"),

  // System Errors
  OPERATION_FAILED("OPERATION_FAILED", "Operation failed to complete"),
  EXTERNAL_SERVICE_ERROR("EXTERNAL_SERVICE_ERROR", "External service error occurred"),
  DATABASE_ERROR("DATABASE_ERROR", "Database operation failed");

  private final String code;
  private final String message;
}

