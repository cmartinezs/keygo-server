package io.cmartinezs.keygo.supabase.user.mapper;

import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;

/**
 * Mapper between User domain model and TenantUserEntity JPA model.
 * <p>Mapper entre el modelo de dominio User y el modelo JPA TenantUserEntity.
 * @author cmartinezs
 * @version 1.0
 */
public class UserPersistenceMapper {

  /**
   * Convert a domain User to a JPA TenantUserEntity.
   * <p>Convierte un User de dominio a un TenantUserEntity JPA.
   * Uses a proxy TenantEntity with only the id set (avoids loading the full tenant).
   * <p>Usa un proxy TenantEntity con solo el id (evita cargar el tenant completo).
   * @param user the domain user
   * @return the corresponding JPA entity
   */
  public TenantUserEntity toEntity(User user) {
    // Proxy tenant with only the id — JPA resolves the FK without a full fetch
    TenantEntity tenantProxy = TenantEntity.builder()
        .id(user.getTenantId().value())
        .build();

    return TenantUserEntity.builder()
        .id(user.getId().value())
        .tenant(tenantProxy)
        .username(user.getUsername().value())
        .email(user.getEmail().value())
        .passwordHash(user.getPasswordHash().value())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .status(user.getStatus())
        .build();
  }

  /**
   * Convert a JPA TenantUserEntity to a domain User.
   * <p>Convierte un TenantUserEntity JPA a un User de dominio.
   * @param entity the JPA entity
   * @return the corresponding domain user
   */
  public User toDomain(TenantUserEntity entity) {
    return User.builder()
        .id(UserId.of(entity.getId()))
        .tenantId(TenantId.of(entity.getTenant().getId()))
        .username(Username.of(entity.getUsername()))
        .email(EmailAddress.of(entity.getEmail()))
        .passwordHash(PasswordHash.of(entity.getPasswordHash()))
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .status(entity.getStatus())
        .build();
  }
}

