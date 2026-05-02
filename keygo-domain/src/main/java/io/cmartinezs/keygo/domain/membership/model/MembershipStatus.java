package io.cmartinezs.keygo.domain.membership.model;

/**
 * Enumeration of possible membership statuses.
 * <p>Enumeración de posibles estados de membresía.
 * @author cmartinezs
 * @version 1.0
 */
public enum MembershipStatus {
  /* User has active access to the app */
  ACTIVE,
  /* User access to the app is temporarily suspended */
  SUSPENDED,
  /* Initial state, pending activation or approval */
  PENDING
}

