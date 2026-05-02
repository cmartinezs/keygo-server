package io.cmartinezs.keygo.domain.membership.model;

import java.util.List;
import java.util.UUID;

/**
 * Value object: entrada de acceso del usuario a una aplicación concreta.
 *
 * <p>Contiene el contexto de membresía enriquecido con el nombre de la app y
 * los roles directamente asignados, listo para presentar en la vista self-service.
 *
 * @param clientAppId   UUID de la aplicación cliente
 * @param clientAppName nombre legible de la aplicación cliente
 * @param membershipId  UUID de la membresía
 * @param status        estado de la membresía (ACTIVE, SUSPENDED, …)
 * @param roles         lista de códigos de rol directos asignados (no jerarquía expandida)
 */
public record UserAccessEntry(
    UUID clientAppId,
    String clientAppName,
    UUID membershipId,
    String status,
    List<String> roles) {}
