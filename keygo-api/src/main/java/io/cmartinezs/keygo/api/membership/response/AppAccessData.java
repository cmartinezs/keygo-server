package io.cmartinezs.keygo.api.membership.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * Response DTO for a single app membership entry within a platform account access view.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Builder
public record AppAccessData(
    @JsonProperty("membership_id") UUID membershipId,
    @JsonProperty("membership_status") String membershipStatus,
    @JsonProperty("client_app_id") UUID clientAppId,
    @JsonProperty("client_id") String clientId,
    @JsonProperty("app_name") String appName,
    List<String> roles) {}
