package io.cmartinezs.keygo.app.auth.port;

import io.cmartinezs.keygo.domain.auth.model.SigningKey;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import java.util.List;
import java.util.Optional;

/**
 * Puerto OUT: persistencia de claves de firma RSA.
 */
public interface SigningKeyRepositoryPort {

  /**
   * Busca la clave de firma activa global (sin scope de tenant).
   * Utilizado por el bootstrap/inicializador.
   */
  Optional<SigningKey> findActiveKey();

  /**
   * Busca la clave ACTIVE para el tenant indicado.
   * Si no existe clave tenant-específica, hace fallback a la clave global (tenant_id IS NULL).
   *
   * @param tenantId tenant para el que se busca la clave
   * @return clave activa encontrada, o empty si no hay ninguna
   */
  Optional<SigningKey> findActiveKeyForTenant(TenantId tenantId);

  /**
   * Busca todas las claves publicables (ACTIVE + RETIRED) globalmente.
   * Usado por los use cases de verificación de tokens (backward compat).
   */
  List<SigningKey> findPublishableKeys();

  /**
   * Busca las claves publicables (ACTIVE + RETIRED) del tenant indicado más las globales.
   * Usado por el JWKS endpoint y la verificación tenant-aware.
   *
   * @param tenantId tenant para el que se publican las claves
   * @return claves tenant-específicas + globales
   */
  List<SigningKey> findPublishableKeysForTenant(TenantId tenantId);

  /**
   * Persiste una clave de firma (nuevo registro o actualización).
   *
   * @param key clave a guardar
   * @return clave guardada
   */
  SigningKey save(SigningKey key);
}
