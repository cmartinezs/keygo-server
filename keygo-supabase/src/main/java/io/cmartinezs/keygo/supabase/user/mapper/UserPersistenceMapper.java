package io.cmartinezs.keygo.supabase.user.mapper;
import io.cmartinezs.keygo.domain.tenant.model.TenantId;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.User;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.Username;
import io.cmartinezs.keygo.supabase.tenant.entity.TenantEntity;
import io.cmartinezs.keygo.supabase.user.entity.TenantUserEntity;
import java.time.LocalDate;
/**
 * Mapper between User domain model and TenantUserEntity JPA model.
 * @author cmartinezs
 * @version 1.1
 */
public class UserPersistenceMapper {
  public TenantUserEntity toEntity(User user) {
    TenantEntity tenantProxy = TenantEntity.builder()
        .id(user.getTenantId().value())
        .build();
    LocalDate birthdate = user.getBirthdate() != null
        ? LocalDate.parse(user.getBirthdate())
        : null;
    return TenantUserEntity.builder()
        .tenant(tenantProxy)
        .username(user.getUsername().value())
        .email(user.getEmail().value())
        .passwordHash(user.getPasswordHash().value())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .status(user.getStatus())
        .phoneNumber(user.getPhoneNumber())
        .locale(user.getLocale())
        .zoneinfo(user.getZoneinfo())
        .profilePictureUrl(user.getProfilePictureUrl())
        .birthdate(birthdate)
        .website(user.getWebsite())
        .build();
  }
  public User toDomain(TenantUserEntity entity) {
    String birthdate = entity.getBirthdate() != null
        ? entity.getBirthdate().toString()
        : null;
    return User.builder()
        .id(UserId.of(entity.getId()))
        .tenantId(TenantId.of(entity.getTenant().getId()))
        .username(Username.of(entity.getUsername()))
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
}
