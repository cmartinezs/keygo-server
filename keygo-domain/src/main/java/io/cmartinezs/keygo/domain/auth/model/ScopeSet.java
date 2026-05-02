package io.cmartinezs.keygo.domain.auth.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;

/**
 * Value object que representa el conjunto de scopes solicitados en la autorización.
 *
 * <p>Scopes estándar soportados:
 *
 * <ul>
 *   <li>openid — OIDC base
 *   <li>profile — información de perfil del usuario
 *   <li>email — correo electrónico del usuario
 * </ul>
 *
 * <p>Este modelo es extensible. Nuevos scopes pueden agregarse en el futuro.
 */
@Getter
public final class ScopeSet {
  private final Set<String> scopes;

  private ScopeSet(Set<String> scopes) {
    this.scopes = Collections.unmodifiableSet(new LinkedHashSet<>(scopes));
  }

  /**
   * Crea un ScopeSet a partir de una cadena de scopes separados por espacios.
   *
   * <p>Ejemplo: "openid profile email"
   *
   * @param scopeString cadena separada por espacios
   * @return ScopeSet
   * @throws IllegalArgumentException si scopeString está vacío o contiene scopes desconocidos
   */
  public static ScopeSet from(String scopeString) {
    if (scopeString == null || scopeString.trim().isEmpty()) {
      throw new IllegalArgumentException("Scope string cannot be null or empty");
    }

    String[] parts = scopeString.trim().split("\\s+");
    Set<String> scopeSet = new LinkedHashSet<>();

    for (String scope : parts) {
      if (!isValidScope(scope)) {
        throw new IllegalArgumentException("Unknown or invalid scope: " + scope);
      }
      scopeSet.add(scope);
    }

    return new ScopeSet(scopeSet);
  }

  /**
   * Crea un ScopeSet a partir de un Set de scopes.
   *
   * @param scopes Set de scopes
   * @return ScopeSet
   * @throws IllegalArgumentException si algún scope es desconocido
   */
  public static ScopeSet of(Set<String> scopes) {
    if (scopes == null || scopes.isEmpty()) {
      throw new IllegalArgumentException("Scopes cannot be null or empty");
    }

    for (String scope : scopes) {
      if (!isValidScope(scope)) {
        throw new IllegalArgumentException("Unknown or invalid scope: " + scope);
      }
    }

    return new ScopeSet(scopes);
  }

  /**
   * Valida si un scope es conocido y permitido.
   *
   * @param scope nombre del scope
   * @return true si es válido, false en caso contrario
   */
  private static boolean isValidScope(String scope) {
    return scope != null
        && (scope.equals("openid")
            || scope.equals("profile")
            || scope.equals("email")
            || scope.equals("offline_access"));
  }

  /**
   * Retorna los scopes como cadena separada por espacios.
   *
   * @return cadena de scopes
   */
  public String asString() {
    return String.join(" ", scopes);
  }

  /**
   * Verifica si este ScopeSet contiene un scope específico.
   *
   * @param scope nombre del scope
   * @return true si está contenido, false en caso contrario
   */
  public boolean contains(String scope) {
    return scopes.contains(scope);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ScopeSet that)) return false;
    return scopes.equals(that.scopes);
  }

  @Override
  public int hashCode() {
    return scopes.hashCode();
  }

  @Override
  public String toString() {
    return asString();
  }
}
