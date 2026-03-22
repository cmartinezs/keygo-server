package io.cmartinezs.keygo.supabase.membership.mapper;

import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.supabase.membership.entity.AppRoleEntity;
import io.cmartinezs.keygo.supabase.membership.entity.MembershipEntity;
import lombok.experimental.UtilityClass;

/**
 * Mapper between domain membership models and JPA entities.
 * <p>Mapeador entre modelos de dominio de membresía y entidades JPA.
 * @author cmartinezs
 * @version 1.0
 */
@UtilityClass
public class MembershipPersistenceMapper {

  /* Membership domain ↔ JPA */

  public static Membership toDomain(MembershipEntity entity) {
    return Membership.builder()
        .id(MembershipId.of(entity.getId()))
        .userId(UserId.of(entity.getUser().getId()))
        .clientAppId(ClientAppId.of(entity.getClientApp().getId()))
        .status(entity.getStatus())
        .build();
  }

  public static MembershipEntity toEntity(Membership domain, MembershipEntity entity) {
    if (entity == null) {
      entity = new MembershipEntity();
    }
    entity.setId(domain.getId().value());
    entity.setStatus(domain.getStatus());
    // Note: user and clientApp FK should be set by the adapter before saving
    return entity;
  }

  /* AppRole domain ↔ JPA */

  public static AppRole toDomain(AppRoleEntity entity) {
    return AppRole.builder()
        .id(AppRoleId.of(entity.getId()))
        .clientAppId(ClientAppId.of(entity.getClientApp().getId()))
        .code(RoleCode.of(entity.getCode()))
        .displayName(entity.getDisplayName())
        .description(entity.getDescription())
        .build();
  }

  public static AppRoleEntity toEntity(AppRole domain, AppRoleEntity entity) {
    if (entity == null) {
      entity = new AppRoleEntity();
    }
    entity.setId(domain.getId().value());
    entity.setCode(domain.getCode().value());
    entity.setDisplayName(domain.getDisplayName());
    entity.setDescription(domain.getDescription());
    // Note: clientApp FK should be set by the adapter before saving
    return entity;
  }
}

