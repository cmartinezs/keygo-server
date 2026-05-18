package io.cmartinezs.keygo.app.membership.result;

import java.util.List;
import java.util.UUID;

/**
 * App access entry for a platform account user's membership in a client application.
 *
 * @param membershipId    UUID of the membership
 * @param membershipStatus status of the membership (ACTIVE, SUSPENDED, PENDING)
 * @param clientAppId     internal UUID of the client application
 * @param clientId        OAuth2 client_id string
 * @param appName         display name of the client application
 * @param roles           role codes directly assigned to this membership
 */
public record AppAccessResult(
    UUID membershipId,
    String membershipStatus,
    UUID clientAppId,
    String clientId,
    String appName,
    List<String> roles) {}
