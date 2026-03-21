package io.cmartinezs.keygo.app.tenant.command;

/**
 * Command object carrying the input data required to create a new tenant.
 * <p>Objeto de comando con los datos de entrada necesarios para crear un nuevo tenant.
 * @author cmartinezs
 * @version 1.0
 */
public record CreateTenantCommand(

    /* Human-readable display name of the tenant.
     * Nombre de visualización legible del tenant. */
    String name,

    /* URL-friendly unique identifier (lowercase, alphanumeric + hyphens).
     * Identificador único amigable para URLs (minúsculas, alfanumérico + guiones). */
    String slug,

    /* Email address of the tenant owner.
     * Dirección de email del propietario del tenant. */
    String ownerEmail
) {}

