package io.cmartinezs.keygo.app.user.filter;

import io.cmartinezs.keygo.app.shared.PageFilter;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import java.util.Set;
import lombok.Getter;

/**
 * Filter criteria for listing platform users with pagination and sorting.
 * <p>Criterios de filtro para listar usuarios de plataforma con paginacion y ordenamiento.
 */
@Getter
public class PlatformUserFilter extends PageFilter {

  private static final Set<String> ALLOWED_SORT_FIELDS =
      Set.of("username", "email", "status", "createdAt", "firstName", "lastName");

  private final UserStatus status;
  private final String usernameLike;
  private final String emailLike;

  private PlatformUserFilter(
      UserStatus status,
      String usernameLike,
      String emailLike,
      int page,
      int size,
      String sortBy,
      String sortOrder) {
    super(page, size, sortBy, sortOrder, ALLOWED_SORT_FIELDS);
    this.status = status;
    this.usernameLike = usernameLike != null && usernameLike.isBlank() ? null : usernameLike;
    this.emailLike = emailLike != null && emailLike.isBlank() ? null : emailLike;
  }

  public static PlatformUserFilter of(
      UserStatus status,
      String usernameLike,
      String emailLike,
      int page,
      int size,
      String sortBy,
      String sortOrder) {
    return new PlatformUserFilter(status, usernameLike, emailLike, page, size, sortBy, sortOrder);
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
