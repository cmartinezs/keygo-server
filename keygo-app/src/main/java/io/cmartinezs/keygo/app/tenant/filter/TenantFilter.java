package io.cmartinezs.keygo.app.tenant.filter;

import io.cmartinezs.keygo.domain.tenant.model.TenantStatus;

/**
 * Filter criteria for listing tenants.
 * <p>Criterios de filtro para listar tenants.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class TenantFilter {

  /** Optional status filter. Null means "any status". */
  private final TenantStatus status;

  /** Optional partial match on name (case-insensitive). Null or blank means "no filter". */
  private final String nameLike;

  /** Zero-based page number. Default 0. */
  private final int page;

  /** Page size. Default 20. */
  private final int size;

  private TenantFilter(TenantStatus status, String nameLike, int page, int size) {
    if (page < 0) throw new IllegalArgumentException("Page must be >= 0");
    if (size < 1 || size > 200) throw new IllegalArgumentException("Size must be between 1 and 200");
    this.status = status;
    this.nameLike = (nameLike != null && nameLike.isBlank()) ? null : nameLike;
    this.page = page;
    this.size = size;
  }

  /**
   * Create a TenantFilter with all optional criteria.
   *
   * @param status   optional status filter
   * @param nameLike optional partial name filter (case-insensitive)
   * @param page     zero-based page index (must be &ge; 0)
   * @param size     page size (1–200)
   * @return a new TenantFilter
   */
  public static TenantFilter of(TenantStatus status, String nameLike, int page, int size) {
    return new TenantFilter(status, nameLike, page, size);
  }

  /** Optional status filter. Null means "any status". */
  public TenantStatus getStatus() {
    return status;
  }

  /** Optional partial match on name. Null means "no filter". */
  public String getNameLike() {
    return nameLike;
  }

  /** Zero-based page number. */
  public int getPage() {
    return page;
  }

  /** Page size. */
  public int getSize() {
    return size;
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

