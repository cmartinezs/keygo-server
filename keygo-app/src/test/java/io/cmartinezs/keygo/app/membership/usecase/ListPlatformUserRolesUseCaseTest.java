package io.cmartinezs.keygo.app.membership.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.cmartinezs.keygo.app.billing.contractor.port.ContractorRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.port.PlatformUserRoleRepositoryPort;
import io.cmartinezs.keygo.app.membership.result.PlatformUserRoleResult;
import io.cmartinezs.keygo.domain.billing.contractor.model.Contractor;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorStatus;
import io.cmartinezs.keygo.domain.billing.contractor.model.ContractorType;
import io.cmartinezs.keygo.app.user.port.PlatformUserRepositoryPort;
import io.cmartinezs.keygo.domain.membership.model.PlatformRole;
import io.cmartinezs.keygo.domain.membership.model.PlatformRoleId;
import io.cmartinezs.keygo.domain.membership.model.PlatformUserRole;
import io.cmartinezs.keygo.domain.membership.model.PlatformUserRoleId;
import io.cmartinezs.keygo.domain.user.exception.UserNotFoundException;
import io.cmartinezs.keygo.domain.user.model.EmailAddress;
import io.cmartinezs.keygo.domain.user.model.PasswordHash;
import io.cmartinezs.keygo.domain.user.model.PlatformUser;
import io.cmartinezs.keygo.domain.user.model.UserId;
import io.cmartinezs.keygo.domain.user.model.UserStatus;
import io.cmartinezs.keygo.domain.user.model.Username;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListPlatformUserRolesUseCase")
class ListPlatformUserRolesUseCaseTest {

  @Mock private PlatformUserRepositoryPort platformUserRepositoryPort;
  @Mock private PlatformUserRoleRepositoryPort platformUserRoleRepositoryPort;
  @Mock private PlatformRoleRepositoryPort platformRoleRepositoryPort;
  @Mock private ContractorRepositoryPort contractorRepositoryPort;

  private ListPlatformUserRolesUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase =
        new ListPlatformUserRolesUseCase(
            platformUserRepositoryPort,
            platformUserRoleRepositoryPort,
            platformRoleRepositoryPort,
            contractorRepositoryPort);
  }

  @Test
  @DisplayName("execute: throws UserNotFoundException when platform user does not exist")
  void execute_throwsUserNotFoundException() {
    UUID userId = UUID.randomUUID();
    when(platformUserRepositoryPort.findById(UserId.of(userId))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(userId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(userId.toString());
  }

  @Test
  @DisplayName("execute: returns enriched assigned roles")
  void execute_returnsEnrichedAssignedRoles() {
    UUID userId = UUID.randomUUID();
    UUID assignmentId = UUID.randomUUID();
    UUID roleId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();
    UUID tenantId = UUID.randomUUID();
    Instant assignedAt = Instant.parse("2026-04-13T08:00:00Z");

    PlatformUser user =
        PlatformUser.builder()
            .id(UserId.of(userId))
            .username(Username.of("platform.admin"))
            .email(EmailAddress.of("admin@test.com"))
            .passwordHash(PasswordHash.of("$2a$10$hashedpassword"))
            .firstName("Platform")
            .lastName("Admin")
            .status(UserStatus.ACTIVE)
            .build();
    PlatformUserRole assignment =
        PlatformUserRole.builder()
            .id(PlatformUserRoleId.of(assignmentId))
            .userId(UserId.of(userId))
            .platformRoleId(PlatformRoleId.of(roleId))
            .scopeType("CONTRACTOR")
            .contractorId(contractorId)
            .tenantId(tenantId)
            .assignedAt(assignedAt)
            .build();
    PlatformRole role =
        PlatformRole.builder()
            .id(PlatformRoleId.of(roleId))
            .code("keygo_admin")
            .name("KeyGo Admin")
            .description("Full administrative access")
            .build();
    Contractor contractor =
        Contractor.builder()
            .id(contractorId)
            .primaryContactPlatformUserId(userId)
            .type(ContractorType.COMPANY)
            .displayName("Acme SpA")
            .billingEmail("billing@acme.cl")
            .status(ContractorStatus.ACTIVE)
            .createdAt(OffsetDateTime.parse("2026-04-10T10:15:30Z"))
            .build();

    when(platformUserRepositoryPort.findById(UserId.of(userId))).thenReturn(Optional.of(user));
    when(platformUserRoleRepositoryPort.findByPlatformUserId(userId)).thenReturn(List.of(assignment));
    when(platformRoleRepositoryPort.findAll()).thenReturn(List.of(role));
    when(contractorRepositoryPort.findById(contractorId)).thenReturn(Optional.of(contractor));

    List<PlatformUserRoleResult> result = useCase.execute(userId);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().assignmentId()).isEqualTo(assignmentId);
    assertThat(result.getFirst().roleId()).isEqualTo(roleId);
    assertThat(result.getFirst().roleCode()).isEqualTo("keygo_admin");
    assertThat(result.getFirst().roleName()).isEqualTo("KeyGo Admin");
    assertThat(result.getFirst().description()).isEqualTo("Full administrative access");
    assertThat(result.getFirst().scopeType()).isEqualTo("CONTRACTOR");
    assertThat(result.getFirst().contractorId()).isEqualTo(contractorId);
    assertThat(result.getFirst().tenantId()).isEqualTo(tenantId);
    assertThat(result.getFirst().contractor()).isNotNull();
    assertThat(result.getFirst().contractor().displayName()).isEqualTo("Acme SpA");
    assertThat(result.getFirst().contractor().billingEmail()).isEqualTo("billing@acme.cl");
    assertThat(result.getFirst().assignedAt()).isEqualTo(assignedAt);
    verify(platformUserRoleRepositoryPort).findByPlatformUserId(eq(userId));
  }

  @Test
  @DisplayName("execute: throws IllegalStateException when assignment role metadata is missing")
  void execute_throwsIllegalStateExceptionWhenRoleMetadataMissing() {
    UUID userId = UUID.randomUUID();
    UUID roleId = UUID.randomUUID();
    PlatformUser user =
        PlatformUser.builder()
            .id(UserId.of(userId))
            .username(Username.of("platform.admin"))
            .email(EmailAddress.of("admin@test.com"))
            .passwordHash(PasswordHash.of("$2a$10$hashedpassword"))
            .firstName("Platform")
            .lastName("Admin")
            .status(UserStatus.ACTIVE)
            .build();
    PlatformUserRole assignment =
        PlatformUserRole.builder()
            .id(PlatformUserRoleId.of(UUID.randomUUID()))
            .userId(UserId.of(userId))
            .platformRoleId(PlatformRoleId.of(roleId))
            .assignedAt(Instant.parse("2026-04-13T08:00:00Z"))
            .build();

    when(platformUserRepositoryPort.findById(UserId.of(userId))).thenReturn(Optional.of(user));
    when(platformUserRoleRepositoryPort.findByPlatformUserId(userId)).thenReturn(List.of(assignment));
    when(platformRoleRepositoryPort.findAll()).thenReturn(List.of());

    assertThatThrownBy(() -> useCase.execute(userId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(roleId.toString());
    verifyNoInteractions(contractorRepositoryPort);
  }

  @Test
  @DisplayName("execute: throws IllegalStateException when contractor metadata is missing")
  void execute_throwsIllegalStateExceptionWhenContractorMetadataMissing() {
    UUID userId = UUID.randomUUID();
    UUID roleId = UUID.randomUUID();
    UUID contractorId = UUID.randomUUID();
    PlatformUser user =
        PlatformUser.builder()
            .id(UserId.of(userId))
            .username(Username.of("platform.admin"))
            .email(EmailAddress.of("admin@test.com"))
            .passwordHash(PasswordHash.of("$2a$10$hashedpassword"))
            .firstName("Platform")
            .lastName("Admin")
            .status(UserStatus.ACTIVE)
            .build();
    PlatformUserRole assignment =
        PlatformUserRole.builder()
            .id(PlatformUserRoleId.of(UUID.randomUUID()))
            .userId(UserId.of(userId))
            .platformRoleId(PlatformRoleId.of(roleId))
            .scopeType("CONTRACTOR")
            .contractorId(contractorId)
            .assignedAt(Instant.parse("2026-04-13T08:00:00Z"))
            .build();
    PlatformRole role =
        PlatformRole.builder()
            .id(PlatformRoleId.of(roleId))
            .code("keygo_admin")
            .name("KeyGo Admin")
            .description("Full administrative access")
            .build();

    when(platformUserRepositoryPort.findById(UserId.of(userId))).thenReturn(Optional.of(user));
    when(platformUserRoleRepositoryPort.findByPlatformUserId(userId)).thenReturn(List.of(assignment));
    when(contractorRepositoryPort.findById(contractorId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute(userId))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(contractorId.toString());
  }
}
