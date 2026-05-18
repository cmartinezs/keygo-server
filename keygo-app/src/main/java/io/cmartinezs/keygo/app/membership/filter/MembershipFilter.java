package io.cmartinezs.keygo.app.membership.filter;

import io.cmartinezs.keygo.app.shared.PageFilter;
import io.cmartinezs.keygo.domain.membership.model.MembershipStatus;
import java.util.Set;
import java.util.UUID;

/**
 * Filter criteria for listing memberships with pagination and sorting.
 * <p>Criterios de filtro para listar memberships con paginación y ordenamiento.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class MembershipFilter extends PageFilter {

  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
      "createdAt"
  );

  private final UUID userId;               // optional user ID filter
  private final UUID clientAppId;          // optional client app ID filter
  private final MembershipStatus status;   // optional status filter

  private MembershipFilter(UUID userId, UUID clientAppId, MembershipStatus status,
                           int page, int size, String sortBy, String sortOrder) {
    super(page, size, sortBy, sortOrder, ALLOWED_SORT_FIELDS);
    this.userId = userId;
    this.clientAppId = clientAppId;
    this.status = status;
  }

  /**
   * Create a MembershipFilter with all optional criteria.
   *
   * @param userId optional user ID filter (null = any user)
   * @param clientAppId optional client app ID filter (null = any app)
   * @param page zero-based page number
   * @param size page size (1–200)
   * @param sortBy sortable field name or null
   * @param sortOrder "ASC" or "DESC"
   * @return a new MembershipFilter
   */
  public static MembershipFilter of(UUID userId, UUID clientAppId,
                                    int page, int size, String sortBy, String sortOrder) {
    return new MembershipFilter(userId, clientAppId, null, page, size, sortBy, sortOrder);
  }

  public static MembershipFilter of(UUID userId, UUID clientAppId, MembershipStatus status,
                                    int page, int size, String sortBy, String sortOrder) {
    return new MembershipFilter(userId, clientAppId, status, page, size, sortBy, sortOrder);
  }

  public UUID getUserId() {
    return userId;
  }

  public UUID getClientAppId() {
    return clientAppId;
  }

  public MembershipStatus getStatus() {
    return status;
  }

  public boolean hasUserId() {
    return userId != null;
  }

  public boolean hasClientAppId() {
    return clientAppId != null;
  }

  public boolean hasStatus() {
    return status != null;
  }
}
