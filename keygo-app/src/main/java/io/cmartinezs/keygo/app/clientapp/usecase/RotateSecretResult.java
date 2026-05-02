package io.cmartinezs.keygo.app.clientapp.usecase;

import io.cmartinezs.keygo.domain.clientapp.model.ClientApp;

/**
 * Result of the RotateClientSecret use case — includes the new raw secret.
 * <p>Resultado del caso de uso RotateClientSecret — incluye el nuevo secret en texto plano.
 * @author cmartinezs
 * @version 1.0
 */
public record RotateSecretResult(ClientApp app, String newRawSecret) {}

