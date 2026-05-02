package io.cmartinezs.keygo.app.shared;

import io.cmartinezs.keygo.app.shared.exception.InvalidPaginationParamException;
import java.util.Set;
import lombok.Getter;

/**
 * Abstract base class for paginated and sorted filter criteria.
 * <p>Clase base abstracta para criterios de filtro con paginación y ordenamiento.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public abstract class PageFilter {

  private final int page;
  private final int size;
  private final String sortBy;     // field name, e.g. "username" or null
  private final String sortOrder;  // "ASC" or "DESC"

  /**
   * Protected constructor for subclasses.
   * Validates pagination parameters and sort field against allowed fields.
   *
   * @param page zero-based page number
   * @param size page size (1-200)
   * @param sortBy sort field name (null allowed)
   * @param sortOrder "ASC" or "DESC" (null defaults to "ASC")
   * @param allowedSortFields set of sortable field names
   * @throws InvalidPaginationParamException if validation fails
   */
  protected PageFilter(int page, int size, String sortBy, String sortOrder, Set<String> allowedSortFields) {
    if (page < 0) {
      throw new InvalidPaginationParamException("page", "must be >= 0");
    }
    if (size < 1 || size > 200) {
      throw new InvalidPaginationParamException("size", "must be between 1 and 200");
    }
    if (sortBy != null && !sortBy.isBlank() && !allowedSortFields.contains(sortBy)) {
      throw new InvalidPaginationParamException("sort", "not sortable by: " + sortBy);
    }

    this.page = page;
    this.size = size;
    this.sortBy = (sortBy == null || sortBy.isBlank()) ? null : sortBy;
    this.sortOrder = (sortOrder == null || sortOrder.isBlank()) ? "ASC" : sortOrder.toUpperCase();
  }

    public boolean hasSorting() {
    return sortBy != null;
  }
}
