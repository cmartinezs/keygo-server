package io.cmartinezs.keygo.supabase.user.mapper;

import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.supabase.user.entity.PlatformUserEntity;

/**
 * Mapper between PlatformUser domain model and PlatformUserEntity JPA model.
 * <p>Mapper entre el modelo de dominio PlatformUser y la entidad JPA PlatformUserEntity.
 *
 * @author cmartinezs
 * @version 1.0
 */
public class PlatformUserPersistenceMapper {

  public PlatformUserEntity toEntity(PlatformUser user) {
    PlatformUserEntity.PlatformUserEntityBuilder builder = PlatformUserEntity.builder()
        .email(user.getEmail().value())
        .passwordHash(user.getPasswordHash().value())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .displayName(user.getUsername().value())
        .status(user.getStatus().name())
        .phoneNumber(user.getPhoneNumber())
        .locale(user.getLocale())
        .zoneinfo(user.getZoneinfo())
        .profilePictureUrl(user.getProfilePictureUrl());

    if (user.getId() != null) {
      builder.id(user.getId().value());
    }

    return builder.build();
  }

  public PlatformUser toDomain(PlatformUserEntity entity) {
    return PlatformUser.builder()
        .id(UserId.of(entity.getId()))
        .username(Username.of(resolveUsername(entity)))
        .email(EmailAddress.of(entity.getEmail()))
        .passwordHash(PasswordHash.of(entity.getPasswordHash()))
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .status(UserStatus.valueOf(entity.getStatus()))
        .phoneNumber(entity.getPhoneNumber())
        .locale(entity.getLocale())
        .zoneinfo(entity.getZoneinfo())
        .profilePictureUrl(entity.getProfilePictureUrl())
        .build();
  }

  private String resolveUsername(PlatformUserEntity entity) {
    if (entity.getDisplayName() != null && !entity.getDisplayName().isBlank()) {
      String candidate = entity.getDisplayName().replaceAll("[^a-zA-Z0-9_.\\-]", ".");
      if (candidate.length() >= 3) {
        return candidate;
      }
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
