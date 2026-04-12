package io.cmartinezs.keygo.app.tenant.command;

/**
 * Command object carrying the input data required to create a new tenant.
 * The slug is automatically generated from the name by the use case.
 * <p>Objeto de comando con los datos de entrada necesarios para crear un nuevo tenant.
 * El slug se genera automáticamente a partir del nombre en el caso de uso.
 * @author cmartinezs
 * @version 1.0
 */
public record CreateTenantCommand(

    /* Human-readable display name of the tenant.
     * Nombre de visualización legible del tenant. */
    String name
) {}
