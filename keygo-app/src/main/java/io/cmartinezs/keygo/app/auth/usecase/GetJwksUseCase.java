package io.cmartinezs.keygo.app.auth.usecase;

import io.cmartinezs.keygo.app.auth.port.JwksBuilderPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.app.tenant.port.TenantRepositoryPort;
import io.cmartinezs.keygo.domain.tenant.exception.TenantNotFoundException;
import io.cmartinezs.keygo.domain.tenant.model.TenantSlug;
import java.util.Map;

/**
 * Caso de uso: obtener el JWK Set con las claves públicas publicables.
 *
 * <p>Cuando se invoca con {@code tenantSlug}, retorna solo las claves de ese tenant
 * más las globales (tenant_id IS NULL). De esta forma el JWKS es tenant-aware.
 */
public class GetJwksUseCase {

  private final SigningKeyRepositoryPort signingKeyRepository;
  private final JwksBuilderPort jwksBuilder;
  private final TenantRepositoryPort tenantRepository;

  public GetJwksUseCase(
      SigningKeyRepositoryPort signingKeyRepository,
      JwksBuilderPort jwksBuilder,
      TenantRepositoryPort tenantRepository) {
    this.signingKeyRepository = signingKeyRepository;
    this.jwksBuilder = jwksBuilder;
    this.tenantRepository = tenantRepository;
  }

  /**
   * Obtiene el JWK Set para el endpoint {@code /.well-known/jwks.json} de un tenant concreto.
   *
   * @param tenantSlug slug del tenant (requerido)
   * @return mapa con estructura {@code {"keys": [...]}} listo para serializar a JSON
   * @throws TenantNotFoundException si el tenant no existe
   */
  public Map<String, Object> execute(String tenantSlug) {
    var tenant = tenantRepository.findBySlug(new TenantSlug(tenantSlug))
        .orElseThrow(() -> new TenantNotFoundException(tenantSlug));
    var keys = signingKeyRepository.findPublishableKeysForTenant(tenant.getId());
    return jwksBuilder.buildJwkSet(keys);
  }

  /**
   * Obtiene el JWK Set global (todas las claves publicables sin filtro de tenant).
   * Mantenido para retrocompatibilidad con tests y código existente.
   *
   * @return mapa con estructura {@code {"keys": [...]}}
   */
  public Map<String, Object> execute() {
    var keys = signingKeyRepository.findPublishableKeys();
    return jwksBuilder.buildJwkSet(keys);
  }
}
