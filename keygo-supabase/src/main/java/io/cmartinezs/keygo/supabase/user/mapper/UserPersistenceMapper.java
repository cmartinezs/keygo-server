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
    var platformUser = requirePlatformUser(entity);
    String birthdate = platformUser.getBirthdate() != null
        ? platformUser.getBirthdate().toString()
        : null;

    return User.builder()
        .id(UserId.of(entity.getId()))
        .tenantId(TenantId.of(entity.getTenant().getId()))
        .username(Username.of(resolveUsername(entity)))
        .email(EmailAddress.of(platformUser.getEmail()))
        .passwordHash(PasswordHash.of(platformUser.getPasswordHash()))
        .firstName(platformUser.getFirstName())
        .lastName(platformUser.getLastName())
        .status(entity.getStatus())
        .phoneNumber(platformUser.getPhoneNumber())
        .locale(platformUser.getLocale())
        .zoneinfo(platformUser.getZoneinfo())
        .profilePictureUrl(platformUser.getProfilePictureUrl())
        .birthdate(birthdate)
        .website(platformUser.getWebsite())
        .platformUserId(entity.getPlatformUser() != null ? entity.getPlatformUser().getId() : null)
        .build();
  }

  private String resolveUsername(TenantUserEntity entity) {
    if (entity.getLocalUsername() != null && !entity.getLocalUsername().isBlank()) {
      return entity.getLocalUsername();
    }

    String email = requirePlatformUser(entity).getEmail();
    if (email != null && email.contains("@")) {
      String candidate = email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9_.\\-]", ".");
      if (candidate.length() >= 3) {
        return candidate;
      }
    }

    return "user." + entity.getId().toString().substring(0, 8);
  }

  private io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity requirePlatformUser(
      TenantUserEntity entity) {
    if (entity.getPlatformUser() == null) {
      throw new IllegalStateException(
          "TenantUserEntity " + entity.getId() + " is missing platformUser");
    }
    return entity.getPlatformUser();
  }
}
