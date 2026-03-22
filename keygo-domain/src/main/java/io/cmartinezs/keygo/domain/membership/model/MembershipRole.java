package io.cmartinezs.keygo.domain.membership.model;

/**
 * Value object representing a role assignment within a membership.
 * <p>Objeto de valor que representa la asignación de un rol dentro de una membresía.
 * @author cmartinezs
 * @version 1.0
 */
public record MembershipRole(AppRoleId roleId) {

  public MembershipRole {
    if (roleId == null) {
      throw new IllegalArgumentException("MembershipRole roleId cannot be null");
    }
  }

  public static MembershipRole of(AppRoleId roleId) {
    return new MembershipRole(roleId);
  }

  @Override
  public String toString() {
    return "MembershipRole[" + roleId + "]";
  }
}

