package io.cmartinezs.keygo.domain.membership.model;

import java.util.UUID;

/**
 * Value object for a membership identifier.
 * <p>Objeto de valor para identificador de membresía.
 * @author cmartinezs
 * @version 1.0
 */
public record MembershipId(UUID value) {

  public MembershipId {
    if (value == null) {
      throw new IllegalArgumentException("MembershipId value cannot be null");
    }
  }

  public static MembershipId generate() {
    return new MembershipId(UUID.randomUUID());
  }

  public static MembershipId of(UUID value) {
    return new MembershipId(value);
  }

  public static MembershipId of(String uuidString) {
    try {
      return new MembershipId(UUID.fromString(uuidString));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
    }
  }

  @Override
  public String toString() {
    return "MembershipId[" + value + "]";
  }
}

