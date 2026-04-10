package io.cmartinezs.keygo.supabase.user.mapper;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;

/**
 * Mapper between User domain model and TenantUserEntity JPA model.
 *
 * <p>The tenant-scoped aggregate is now materialized from two tables:
 * {@code tenant_users} for participation data and {@code platform_users} for global identity,
 * credentials and profile fields.
 */
public class UserPersistenceMapper {

  public User toDomain(TenantUserEntity entity) {
    String birthdate = entity.getBirthdate() != null
        ? entity.getBirthdate().toString()
        : null;

    return User.builder()
        .id(UserId.of(entity.getId()))
        .tenantId(TenantId.of(entity.getTenant().getId()))
        .username(Username.of(resolveUsername(entity)))
        .email(EmailAddress.of(entity.getEmail()))
        .passwordHash(PasswordHash.of(entity.getPasswordHash()))
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .status(entity.getStatus())
        .phoneNumber(entity.getPhoneNumber())
        .locale(entity.getLocale())
        .zoneinfo(entity.getZoneinfo())
        .profilePictureUrl(entity.getProfilePictureUrl())
        .birthdate(birthdate)
        .website(entity.getWebsite())
        .platformUserId(entity.getPlatformUser() != null ? entity.getPlatformUser().getId() : null)
        .build();
  }

  private String resolveUsername(TenantUserEntity entity) {
    if (entity.getUsername() != null && !entity.getUsername().isBlank()) {
      return entity.getUsername();
    }

    String email = entity.getEmail();
    if (email != null && email.contains("@")) {
      String candidate = email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9_.\\-]", ".");
      if (candidate.length() >= 3) {
        return candidate;
      }
    }

    return "user." + entity.getId().toString().substring(0, 8);
  }
}
