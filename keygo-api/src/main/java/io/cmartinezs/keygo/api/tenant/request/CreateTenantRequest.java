package io.cmartinezs.keygo.api.tenant.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new tenant.
 * The slug is automatically derived from the name on the server side.
 * <p>DTO de solicitud para crear un nuevo tenant.
 * El slug se deriva automáticamente del nombre en el servidor.
 *
 * @author cmartinezs
 * @version 1.0
 */
public record CreateTenantRequest(

    @NotBlank(message = "Tenant name is required")
    @Size(max = 255, message = "Tenant name must not exceed 255 characters")
    String name,

    @NotBlank(message = "Owner email is required")
    @Email(message = "Owner email must be a valid email address")
    String ownerEmail
) {}



