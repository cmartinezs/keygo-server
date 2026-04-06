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
        .username(user.getUsername().value())
        .email(user.getEmail().value())
        .passwordHash(user.getPasswordHash().value())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
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
        .username(Username.of(entity.getUsername()))
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
}
