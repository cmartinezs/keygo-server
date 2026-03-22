package io.cmartinezs.keygo.api.user.request;

/**
 * Request body for updating a user's profile information.
 * <p>Cuerpo de solicitud para actualizar la información de perfil de un usuario.
 * All fields are optional — null values are accepted to clear existing data.
 * <p>Todos los campos son opcionales — valores nulos son aceptados para limpiar datos existentes.
 * @param firstName new first name (may be null)
 * @param lastName  new last name (may be null)
 * @author cmartinezs
 * @version 1.0
 */
public record UpdateUserRequest(
    String firstName,
    String lastName
) {
}

