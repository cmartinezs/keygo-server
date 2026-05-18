package io.cmartinezs.keygo.app.clientapp.filter;

import io.cmartinezs.keygo.app.shared.PageFilter;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
import io.cmartinezs.keygo.domain.clientapp.model.ClientType;
import io.cmartinezs.keygo.domain.clientapp.model.RegistrationPolicy;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Filter criteria for listing client apps with pagination and sorting.
 * <p>Criterios de filtro para listar aplicaciones cliente con paginación y ordenamiento.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class ClientAppFilter extends PageFilter {

  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
      "name", "status", "createdAt"
  );

  private final ClientAppStatus status;                                  // optional status filter
  private final String nameLike;                                         // optional name substring
  private final String q;                                                // optional OR search across name and clientId
  private final ClientType type;                                         // optional type filter
  private final Set<RegistrationPolicy> excludeRegistrationPolicies;    // optional policies to exclude

  private ClientAppFilter(ClientAppStatus status, String nameLike, String q, ClientType type,
                          Set<RegistrationPolicy> excludeRegistrationPolicies,
                          int page, int size, String sortBy, String sortOrder) {
    super(page, size, sortBy, sortOrder, ALLOWED_SORT_FIELDS);
    this.status = status;
    this.nameLike = (nameLike != null && nameLike.isBlank()) ? null : nameLike;
    this.q = (q != null && q.isBlank()) ? null : q;
    this.type = type;
    this.excludeRegistrationPolicies = excludeRegistrationPolicies != null
        ? Collections.unmodifiableSet(excludeRegistrationPolicies)
        : Collections.emptySet();
  }

  /**
   * Create a ClientAppFilter with all optional criteria (no policy exclusion).
   *
   * @param status optional status filter (null = any status)
   * @param nameLike optional partial name match (case-insensitive)
   * @param page zero-based page number
   * @param size page size (1–200)
   * @param sortBy sortable field name or null
   * @param sortOrder "ASC" or "DESC"
   * @return a new ClientAppFilter
   */
  public static ClientAppFilter of(ClientAppStatus status, String nameLike,
                                   int page, int size, String sortBy, String sortOrder) {
    return new ClientAppFilter(status, nameLike, null, null, null, page, size, sortBy, sortOrder);
  }

  /**
   * Create a ClientAppFilter with type and registration-policy exclusion criteria.
   * Used by public discovery endpoints that must restrict to PUBLIC apps and exclude non-self-service policies.
   *
   * @param status optional status filter (null = any status)
   * @param nameLike optional partial name match (case-insensitive)
   * @param type optional type filter (null = any type)
   * @param excludeRegistrationPolicies policies to exclude (null or empty = no exclusion)
   * @param page zero-based page number
   * @param size page size (1–200)
   * @param sortBy sortable field name or null
   * @param sortOrder "ASC" or "DESC"
   * @return a new ClientAppFilter
   */
  public static ClientAppFilter of(ClientAppStatus status, String nameLike,
                                   ClientType type, Set<RegistrationPolicy> excludeRegistrationPolicies,
                                   int page, int size, String sortBy, String sortOrder) {
    return new ClientAppFilter(status, nameLike, null, type, excludeRegistrationPolicies,
        page, size, sortBy, sortOrder);
  }

  /**
   * Convenience factory for excluding a vararg of policies.
   */
  public static ClientAppFilter of(ClientAppStatus status, String nameLike,
                                   ClientType type, RegistrationPolicy[] excludePolicies,
                                   int page, int size, String sortBy, String sortOrder) {
    Set<RegistrationPolicy> set = (excludePolicies != null && excludePolicies.length > 0)
        ? EnumSet.copyOf(Arrays.asList(excludePolicies))
        : null;
    return new ClientAppFilter(status, nameLike, null, type, set, page, size, sortBy, sortOrder);
  }

  public static ClientAppFilter of(ClientAppStatus status, String nameLike, String q,
                                   int page, int size, String sortBy, String sortOrder) {
    return new ClientAppFilter(status, nameLike, q, null, null, page, size, sortBy, sortOrder);
  }

  public ClientAppStatus getStatus() {
    return status;
  }

  public String getNameLike() {
    return nameLike;
  }

  public ClientType getType() {
    return type;
  }

  public Set<RegistrationPolicy> getExcludeRegistrationPolicies() {
    return excludeRegistrationPolicies;
  }

  public boolean hasStatus() {
    return status != null;
  }

  public boolean hasNameLike() {
    return nameLike != null;
  }

  public String getQ() {
    return q;
  }

  public boolean hasQ() {
    return q != null;
  }

  public boolean hasType() {
    return type != null;
  }

  public boolean hasExcludeRegistrationPolicies() {
    return !excludeRegistrationPolicies.isEmpty();
  }
}
