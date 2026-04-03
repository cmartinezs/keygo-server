package io.cmartinezs.keygo.app.tenant.filter;

import io.cmartinezs.keygo.app.shared.PageFilter;
import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;
import java.util.Set;
import lombok.Getter;

/**
 * Filter criteria for listing tenants with pagination and sorting.
 * <p>Criterios de filtro para listar tenants con paginación y ordenamiento.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class TenantFilter extends PageFilter {

  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
      "name", "status", "createdAt"
  );

  /** Optional status filter. Null means "any status".
   * -- GETTER --
   * Optional status filter. Null means "any status".
   */
  private final TenantStatus status;

  /** Optional partial match on name (case-insensitive). Null or blank means "no filter".
   * -- GETTER --
   * Optional partial match on name. Null means "no filter".
   */
  private final String nameLike;

  private TenantFilter(TenantStatus status, String nameLike, int page, int size, String sortBy, String sortOrder) {
    super(page, size, sortBy, sortOrder, ALLOWED_SORT_FIELDS);
    this.status = status;
    this.nameLike = (nameLike != null && nameLike.isBlank()) ? null : nameLike;
  }

  /**
   * Create a TenantFilter with all optional criteria.
   *
   * @param status   optional status filter
   * @param nameLike optional partial name filter (case-insensitive)
   * @param page     zero-based page index (must be &ge; 0)
   * @param size     page size (1–200)
   * @param sortBy   sortable field name or null
   * @param sortOrder "ASC" or "DESC"
   * @return a new TenantFilter
   */
  public static TenantFilter of(TenantStatus status, String nameLike, int page, int size, String sortBy, String sortOrder) {
    return new TenantFilter(status, nameLike, page, size, sortBy, sortOrder);
  }

  /**
   * Create a TenantFilter without sorting (backwards compatibility).
   *
   * @param status   optional status filter
   * @param nameLike optional partial name filter (case-insensitive)
   * @param page     zero-based page index (must be &ge; 0)
   * @param size     page size (1–200)
   * @return a new TenantFilter
   * @deprecated Use of(status, nameLike, page, size, sortBy, sortOrder) instead
   */
  @Deprecated(forRemoval = false)
  public static TenantFilter of(TenantStatus status, String nameLike, int page, int size) {
    return new TenantFilter(status, nameLike, page, size, null, null);
  }

    /** Returns true if a status filter is active. */
  public boolean hasStatus() {
    return status != null;
  }

  /** Returns true if a name filter is active. */
  public boolean hasNameLike() {
    return nameLike != null;
  }
}

