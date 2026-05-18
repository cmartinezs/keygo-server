package io.cmartinezs.keygo.domain.clientapp.model;

import io.cmartinezs.keygo.domain.clientapp.exception.InvalidClientAppConfigException;
import java.util.Set;

/**
 * Value object encapsulating the access policy of a client application.
 * Defines which OAuth2 grants and scopes are allowed.
 * <p>Objeto de valor que encapsula la política de acceso de una aplicación cliente.
 * Define qué grants y scopes OAuth2 están permitidos.
 * @author cmartinezs
 * @version 1.0
 */
public record AccessPolicy(Set<AllowedGrant> grants, Set<AllowedScope> scopes) {

  public AccessPolicy {
    if (grants == null || grants.isEmpty()) {
      throw new InvalidClientAppConfigException("AccessPolicy must have at least one allowed grant");
    }
    if (scopes == null) {
      throw new InvalidClientAppConfigException("AccessPolicy scopes cannot be null");
    }
    grants = Set.copyOf(grants);
    scopes = Set.copyOf(scopes);
  }

  /**
   * Check whether the given grant is allowed by this policy.
   * <p>Verifica si el grant dado está permitido por esta política.
   * @param grant the grant to check
   * @return true if the grant is allowed
   */
  public boolean allowsGrant(AllowedGrant grant) {
    return grants.contains(grant);
  }

  /**
   * Check whether the given scope is allowed by this policy.
   * <p>Verifica si el scope dado está permitido por esta política.
   * @param scope the scope to check
   * @return true if the scope is allowed
   */
  public boolean allowsScope(AllowedScope scope) {
    return scopes.contains(scope);
  }
}

