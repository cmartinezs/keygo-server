package io.cmartinezs.keygo.app.clientapp.filter;

import io.cmartinezs.keygo.app.shared.PageFilter;
import io.cmartinezs.keygo.domain.clientapp.model.ClientAppStatus;
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

  private final ClientAppStatus status;    // optional status filter
  private final String nameLike;           // optional name substring

  private ClientAppFilter(ClientAppStatus status, String nameLike,
                          int page, int size, String sortBy, String sortOrder) {
    super(page, size, sortBy, sortOrder, ALLOWED_SORT_FIELDS);
    this.status = status;
    this.nameLike = (nameLike != null && nameLike.isBlank()) ? null : nameLike;
  }

  /**
   * Create a ClientAppFilter with all optional criteria.
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
    return new ClientAppFilter(status, nameLike, page, size, sortBy, sortOrder);
  }

  public ClientAppStatus getStatus() {
    return status;
  }

  public String getNameLike() {
    return nameLike;
  }

  public boolean hasStatus() {
    return status != null;
  }

  public boolean hasNameLike() {
    return nameLike != null;
  }
}
