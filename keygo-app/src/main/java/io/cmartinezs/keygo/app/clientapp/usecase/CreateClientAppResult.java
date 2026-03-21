package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;

/**
 * Result of the CreateClientApp use case — includes raw secret (available only at creation time).
 * <p>Resultado del caso de uso CreateClientApp — incluye el secret en texto plano
 * (disponible solo en el momento de creación).
 * @author cmartinezs
 * @version 1.0
 */
public record CreateClientAppResult(ClientApp app, String rawSecret) {}

