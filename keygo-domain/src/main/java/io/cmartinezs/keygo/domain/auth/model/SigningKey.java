package io.cmartinezs.keygo.domain.auth.model;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Entidad de dominio: clave de firma RSA para emisión de tokens JWT.
 *
 * <p>Una clave ACTIVE es la única usada para firmar nuevos tokens. Las claves RETIRED siguen
 * publicando su clave pública en JWKS para que tokens existentes puedan ser verificados.
 * Las claves REVOKED no participan en ninguna operación.
 *
 * <p>El campo {@code tenantId} es nullable: {@code null} indica una clave global de plataforma
 * compartida por todos los tenants (fallback). Una clave con tenant asociado es exclusiva de ese tenant.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class SigningKey {

  private final SigningKeyId id;

  /** Identificador de clave (Key ID) incluido en el header JWT y en JWKS. */
  private final String kid;

  private final SigningKeyAlgorithm algorithm;
  private final SigningKeyStatus status;

  /**
   * Tenant propietario de esta clave. {@code null} = clave global de plataforma.
   * Una clave con tenant asociado solo se usa/publica para ese tenant.
   */
  private final TenantId tenantId;

  /** Material de clave pública en formato PEM. Siempre presente. */
  private final String publicMaterial;

  /**
   * Material de clave privada en formato PEM.
   *
   * <p>Solo presente cuando la clave fue generada internamente (dev/staging).
   * En producción con KMS externo este campo será {@code null}.
   */
  private final String privateMaterial;

  private final Instant activatedAt;

  /** Instante en que la clave fue retirada. Nulo si está ACTIVE. */
  private final Instant retiredAt;

  /** {@code true} si esta clave puede ser usada para firmar tokens nuevos. */
  public boolean isActive() {
    return SigningKeyStatus.ACTIVE.equals(status);
  }

  /**
   * {@code true} si la clave pública debe publicarse en JWKS.
   *
   * <p>Las claves ACTIVE y RETIRED se publican para permitir validación de tokens existentes.
   */
  public boolean isPublishable() {
    return SigningKeyStatus.ACTIVE.equals(status) || SigningKeyStatus.RETIRED.equals(status);
  }
}
