package io.cmartinezs.keygo.app.role.filter;

import io.cmartinezs.keygo.app.shared.PageFilter;
import java.util.Set;

/**
 * Filter criteria for listing app roles with pagination and sorting.
 * <p>Criterios de filtro para listar roles de aplicación con paginación y ordenamiento.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class AppRoleFilter extends PageFilter {

  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
      "name", "createdAt"
  );

  private final String nameLike;    // optional name substring

  private AppRoleFilter(String nameLike,
                        int page, int size, String sortBy, String sortOrder) {
    super(page, size, sortBy, sortOrder, ALLOWED_SORT_FIELDS);
    this.nameLike = (nameLike != null && nameLike.isBlank()) ? null : nameLike;
  }

  /**
   * Create an AppRoleFilter with all optional criteria.
   *
   * @param nameLike optional partial name match (case-insensitive)
   * @param page zero-based page number
   * @param size page size (1–200)
   * @param sortBy sortable field name or null
   * @param sortOrder "ASC" or "DESC"
   * @return a new AppRoleFilter
   */
  public static AppRoleFilter of(String nameLike,
                                 int page, int size, String sortBy, String sortOrder) {
    return new AppRoleFilter(nameLike, page, size, sortBy, sortOrder);
  }

  public String getNameLike() {
    return nameLike;
  }

  public boolean hasNameLike() {
    return nameLike != null;
  }
}
