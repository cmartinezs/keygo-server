package io.cmartinezs.keygo.supabase.membership.mapper;

import io.cmartinezs.keygo.domain.clientapp.model.ClientAppId;
import io.cmartinezs.keygo.domain.membership.model.AppRole;
import io.cmartinezs.keygo.domain.membership.model.AppRoleId;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.cmartinezs.keygo.domain.membership.model.PlatformRole;
import io.cmartinezs.keygo.domain.membership.model.PlatformRoleId;
import io.cmartinezs.keygo.domain.membership.model.PlatformUserRole;
import io.cmartinezs.keygo.domain.membership.model.PlatformUserRoleId;
import io.cmartinezs.keygo.domain.membership.model.RoleCode;
import io.cmartinezs.keygo.domain.membership.model.TenantRole;
import io.cmartinezs.keygo.domain.membership.model.TenantRoleId;
import io.cmartinezs.keygo.domain.membership.model.TenantUserRole;
import io.cmartinezs.keygo.domain.membership.model.TenantUserRoleId;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.supabase.membership.entity.AppRoleEntity;
import io.cmartinezs.keygo.supabase.membership.entity.MembershipEntity;
import io.cmartinezs.keygo.supabase.membership.entity.PlatformRoleEntity;
import io.cmartinezs.keygo.supabase.membership.entity.PlatformUserRoleEntity;
import io.cmartinezs.keygo.supabase.membership.entity.TenantRoleEntity;
import io.cmartinezs.keygo.supabase.membership.entity.TenantUserRoleEntity;
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
    if (domain.getId() != null) {
      entity.setId(domain.getId().value());
    }
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
    if (domain.getId() != null) {
      entity.setId(domain.getId().value());
    }
    entity.setCode(domain.getCode().value());
    entity.setDisplayName(domain.getDisplayName());
    entity.setDescription(domain.getDescription());
    // Note: clientApp FK should be set by the adapter before saving
    return entity;
  }

  /* PlatformRole domain ↔ JPA */

  public static PlatformRole toDomain(PlatformRoleEntity entity) {
    return PlatformRole.builder()
        .id(PlatformRoleId.of(entity.getId()))
        .code(entity.getCode())
        .name(entity.getName())
        .description(entity.getDescription())
        .build();
  }

  /* PlatformUserRole domain ↔ JPA */

  public static PlatformUserRole toDomain(PlatformUserRoleEntity entity) {
    return PlatformUserRole.builder()
        .id(PlatformUserRoleId.of(entity.getId()))
        .userId(UserId.of(entity.getPlatformUser().getId()))
        .platformRoleId(PlatformRoleId.of(entity.getPlatformRole().getId()))
        .scopeType(entity.getScopeType())
        .contractorId(entity.getContractorId())
        .tenantId(entity.getTenantId())
        .assignedAt(entity.getAssignedAt().toInstant())
        .build();
  }

  /* TenantRole domain ↔ JPA */

  public static TenantRole toDomain(TenantRoleEntity entity) {
    return TenantRole.builder()
        .id(TenantRoleId.of(entity.getId()))
        .tenantId(new TenantId(entity.getTenant().getId()))
        .code(entity.getCode())
        .name(entity.getName())
        .description(entity.getDescription())
        .active(entity.isActive())
        .build();
  }

  /* TenantUserRole domain ↔ JPA */

  public static TenantUserRole toDomain(TenantUserRoleEntity entity) {
    return TenantUserRole.builder()
        .id(TenantUserRoleId.of(entity.getId()))
        .tenantUserId(entity.getTenantUserId())
        .tenantRoleId(TenantRoleId.of(entity.getTenantRoleId()))
        .assignedAt(entity.getAssignedAt().toInstant())
        .build();
  }
}

