package io.cmartinezs.keygo.app.user.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.cmartinezs.keygo.app.shared.exception.InvalidPaginationParamException;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserFilter")
class UserFilterTest {

  @Test
  @DisplayName("of: creates filter with all parameters")
  void of_createsFilterWithAllParameters() {
    // Given / When
    UserFilter filter = UserFilter.of(
        UserStatus.ACTIVE, "john", "example.com",
        0, 20, "username", "ASC");

    // Then
    assertThat(filter.getStatus()).isEqualTo(UserStatus.ACTIVE);
    assertThat(filter.getUsernameLike()).isEqualTo("john");
    assertThat(filter.getEmailLike()).isEqualTo("example.com");
    assertThat(filter.getPage()).isZero();
    assertThat(filter.getSize()).isEqualTo(20);
    assertThat(filter.getSortBy()).isEqualTo("username");
    assertThat(filter.getSortOrder()).isEqualTo("ASC");
  }

  @Test
  @DisplayName("of: defaults status to null when not provided")
  void of_defaultsStatusToNull() {
    // When
    UserFilter filter = UserFilter.of(null, null, null, 0, 20, null, null);

    // Then
    assertThat(filter.getStatus()).isNull();
    assertThat(filter.hasStatus()).isFalse();
  }

  @Test
  @DisplayName("of: trims blank usernameLike to null")
  void of_trimsBlankUsernameLike() {
    // When
    UserFilter filter = UserFilter.of(null, "   ", null, 0, 20, null, null);

    // Then
    assertThat(filter.getUsernameLike()).isNull();
    assertThat(filter.hasUsernameLike()).isFalse();
  }

  @Test
  @DisplayName("of: trims blank emailLike to null")
  void of_trimsBlankEmailLike() {
    // When
    UserFilter filter = UserFilter.of(null, null, "  ", 0, 20, null, null);

    // Then
    assertThat(filter.getEmailLike()).isNull();
    assertThat(filter.hasEmailLike()).isFalse();
  }

  @Test
  @DisplayName("of: defaults sortOrder to ASC when not provided")
  void of_defaultsSortOrderToASC() {
    // When
    UserFilter filter = UserFilter.of(null, null, null, 0, 20, "username", null);

    // Then
    assertThat(filter.getSortOrder()).isEqualTo("ASC");
  }

  @Test
  @DisplayName("of: converts sortOrder to uppercase")
  void of_convertsSortOrderToUppercase() {
    // When
    UserFilter filter = UserFilter.of(null, null, null, 0, 20, "email", "desc");

    // Then
    assertThat(filter.getSortOrder()).isEqualTo("DESC");
  }

  @Test
  @DisplayName("of: rejects negative page")
  void of_rejectsNegativePage() {
    // When / Then
    assertThatThrownBy(() -> UserFilter.of(null, null, null, -1, 20, null, null))
        .isInstanceOf(InvalidPaginationParamException.class)
        .hasMessageContaining("page");
  }

  @Test
  @DisplayName("of: rejects size < 1")
  void of_rejectsSizeLessThan1() {
    // When / Then
    assertThatThrownBy(() -> UserFilter.of(null, null, null, 0, 0, null, null))
        .isInstanceOf(InvalidPaginationParamException.class)
        .hasMessageContaining("size");
  }

  @Test
  @DisplayName("of: rejects size > 200")
  void of_rejectsSizeGreaterThan200() {
    // When / Then
    assertThatThrownBy(() -> UserFilter.of(null, null, null, 0, 201, null, null))
        .isInstanceOf(InvalidPaginationParamException.class)
        .hasMessageContaining("size");
  }

  @Test
  @DisplayName("of: rejects invalid sortBy field")
  void of_rejectsInvalidSortByField() {
    // When / Then
    assertThatThrownBy(() -> UserFilter.of(null, null, null, 0, 20, "invalidField", "ASC"))
        .isInstanceOf(InvalidPaginationParamException.class)
        .hasMessageContaining("sort");
  }

  @Test
  @DisplayName("of: accepts valid sortBy fields")
  void of_acceptsValidSortByFields() {
    // When
    UserFilter filter1 = UserFilter.of(null, null, null, 0, 20, "username", "ASC");
    UserFilter filter2 = UserFilter.of(null, null, null, 0, 20, "email", "ASC");
    UserFilter filter3 = UserFilter.of(null, null, null, 0, 20, "status", "ASC");
    UserFilter filter4 = UserFilter.of(null, null, null, 0, 20, "createdAt", "ASC");

    // Then
    assertThat(filter1.getSortBy()).isEqualTo("username");
    assertThat(filter2.getSortBy()).isEqualTo("email");
    assertThat(filter3.getSortBy()).isEqualTo("status");
    assertThat(filter4.getSortBy()).isEqualTo("createdAt");
  }

  @Test
  @DisplayName("hasSorting: returns true when sortBy is set")
  void hasSorting_returnsTrueWhenSortBySet() {
    // Given
    UserFilter filter = UserFilter.of(null, null, null, 0, 20, "username", "ASC");

    // Then
    assertThat(filter.hasSorting()).isTrue();
  }

  @Test
  @DisplayName("hasSorting: returns false when sortBy is null")
  void hasSorting_returnsFalseWhenSortByNull() {
    // Given
    UserFilter filter = UserFilter.of(null, null, null, 0, 20, null, "ASC");

    // Then
    assertThat(filter.hasSorting()).isFalse();
  }
}
