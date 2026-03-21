package io.cmartinezs.keygo.api.tenant.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new tenant.
 * DTO de solicitud para crear un nuevo tenant.
 *
 * @author cmartinezs
 * @version 1.0
 */
public record CreateTenantRequest(

    @NotBlank(message = "Tenant name is required")
    @Size(max = 255, message = "Tenant name must not exceed 255 characters")
    String name,

    @NotBlank(message = "Tenant slug is required")
    @Size(min = 3, max = 100, message = "Tenant slug must be between 3 and 100 characters")
    @Pattern(
        regexp = "^[a-z0-9][a-z0-9\\-]*[a-z0-9]$|^[a-z0-9]{3,}$",
        message = "Tenant slug must be lowercase alphanumeric with hyphens and cannot start/end with hyphen"
    )
    String slug,

    @NotBlank(message = "Owner email is required")
    @Email(message = "Owner email must be a valid email address")
    String ownerEmail
) {}

