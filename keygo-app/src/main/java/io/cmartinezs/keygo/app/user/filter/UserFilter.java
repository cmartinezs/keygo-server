package io.cmartinezs.keygo.app.user.filter;

import io.cmartinezs.keygo.app.shared.PageFilter;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import java.util.Set;
import lombok.Getter;

/**
 * Filter criteria for listing users with pagination and sorting.
 * <p>Criterios de filtro para listar usuarios con paginación y ordenamiento.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class UserFilter extends PageFilter {

  private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
      "username", "email", "status", "createdAt"
  );

  private final UserStatus status;        // optional status filter
  private final String usernameLike;      // optional username substring
  private final String emailLike;         // optional email substring

  private UserFilter(UserStatus status, String usernameLike, String emailLike,
                     int page, int size, String sortBy, String sortOrder) {
    super(page, size, sortBy, sortOrder, ALLOWED_SORT_FIELDS);
    this.status = status;
    this.usernameLike = (usernameLike != null && usernameLike.isBlank()) ? null : usernameLike;
    this.emailLike = (emailLike != null && emailLike.isBlank()) ? null : emailLike;
  }

  /**
   * Create a UserFilter with all optional criteria.
   *
   * @param status optional status filter (null = any status)
   * @param usernameLike optional partial username match (case-insensitive)
   * @param emailLike optional partial email match (case-insensitive)
   * @param page zero-based page number
   * @param size page size (1–200)
   * @param sortBy sortable field name or null
   * @param sortOrder "ASC" or "DESC"
   * @return a new UserFilter
   */
  public static UserFilter of(UserStatus status, String usernameLike, String emailLike,
                              int page, int size, String sortBy, String sortOrder) {
    return new UserFilter(status, usernameLike, emailLike, page, size, sortBy, sortOrder);
  }

    public boolean hasStatus() {
    return status != null;
  }

  public boolean hasUsernameLike() {
    return usernameLike != null;
  }

  public boolean hasEmailLike() {
    return emailLike != null;
  }
}
