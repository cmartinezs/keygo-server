package io.cmartinezs.keygo.app.auth.result;

import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import java.util.List;

/**
 * Resultado de obtener el JWK Set para el endpoint JWKS.
 *
 * @param keys lista de claves publicables (ACTIVE + RETIRED)
 */
public record JwksResult(List<SigningKey> keys) {}

