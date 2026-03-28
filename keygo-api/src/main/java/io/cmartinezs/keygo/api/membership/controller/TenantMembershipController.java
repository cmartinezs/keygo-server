package io.cmartinezs.keygo.api.membership.controller;

import io.cmartinezs.keygo.api.membership.request.CreateMembershipRequest;
import io.cmartinezs.keygo.api.membership.response.MembershipData;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.app.membership.command.CreateMembershipCommand;
import io.cmartinezs.keygo.app.membership.usecase.CreateMembershipUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListMembershipsUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RevokeMembershipUseCase;
import io.cmartinezs.keygo.domain.membership.model.Membership;
import io.cmartinezs.keygo.domain.membership.model.MembershipId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for membership management within a tenant.
 * <p>Controlador REST para gestión de membresías dentro de un tenant.
 * @author cmartinezs
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/tenants/{tenantSlug}/memberships")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Memberships", description = "User access to applications — requires Bearer JWT")
@PreAuthorize("hasAnyRole('ADMIN','ADMIN_TENANT') and @tenantAuthorizationEvaluator.hasTenantAccess(authentication)")
public class TenantMembershipController {

  private final CreateMembershipUseCase createMembershipUseCase;
  private final ListMembershipsUseCase listMembershipsUseCase;
  private final RevokeMembershipUseCase revokeMembershipUseCase;

  public TenantMembershipController(
      CreateMembershipUseCase createMembershipUseCase,
      ListMembershipsUseCase listMembershipsUseCase,
      RevokeMembershipUseCase revokeMembershipUseCase) {
    this.createMembershipUseCase = createMembershipUseCase;
    this.listMembershipsUseCase = listMembershipsUseCase;
    this.revokeMembershipUseCase = revokeMembershipUseCase;
  }

  @PostMapping
  @Operation(
      summary = "Create a membership",
      description = "Grant user access to an application with specified roles")
  @ApiResponse(responseCode = "201", description = "Membership created",
      content = @Content(schema = @Schema(implementation = MembershipData.Response.class)))
  @ApiResponse(responseCode = "400", description = "Invalid input")
  @ApiResponse(responseCode = "404", description = "User, app, or tenant not found")
  public ResponseEntity<BaseResponse<MembershipData>> createMembership(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Valid @RequestBody CreateMembershipRequest request) {

    CreateMembershipCommand command = new CreateMembershipCommand(
        tenantSlug,
        request.userId(),
        request.clientAppId(),
        request.roleCodes());

    Membership membership = createMembershipUseCase.execute(command);

    MembershipData data = MembershipData.builder()
        .id(membership.getId().value())
        .userId(membership.getUserId().value())
        .clientAppId(membership.getClientAppId().value())
        .status(membership.getStatus())
        .roleIds(membership.getRoles().stream()
            .map(r -> r.roleId().value())
            .collect(java.util.stream.Collectors.toSet()))
        .createdAt(null) // Will be set by entity
        .build();

    BaseResponse<MembershipData> response = BaseResponse.<MembershipData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.MEMBERSHIP_CREATED))
        .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping
  @Operation(
      summary = "List memberships",
      description = "List all memberships for a user or app (query params determine filter)")
  @ApiResponse(responseCode = "200", description = "Memberships retrieved",
      content = @Content(schema = @Schema(implementation = MembershipData.ListResponse.class)))
  @ApiResponse(responseCode = "400", description = "Invalid query parameters")
  public ResponseEntity<BaseResponse<List<MembershipData>>> listMemberships(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Filter by user ID") @RequestParam(name = "user_id", required = false) UUID userId,
      @Parameter(description = "Filter by client app ID") @RequestParam(name = "client_app_id", required = false) UUID clientAppId) {

    List<Membership> memberships;
    if (userId != null) {
      memberships = listMembershipsUseCase.listByUserId(userId, tenantSlug);
    } else if (clientAppId != null) {
      memberships = listMembershipsUseCase.listByClientAppId(clientAppId, tenantSlug);
    } else {
      memberships = List.of();
    }

    List<MembershipData> data = memberships.stream()
        .map(m -> MembershipData.builder()
            .id(m.getId().value())
            .userId(m.getUserId().value())
            .clientAppId(m.getClientAppId().value())
            .status(m.getStatus())
            .roleIds(m.getRoles().stream()
                .map(r -> r.roleId().value())
                .collect(java.util.stream.Collectors.toSet()))
            .build())
        .toList();

    BaseResponse<List<MembershipData>> response = BaseResponse.<List<MembershipData>>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.MEMBERSHIP_LIST_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @DeleteMapping("/{membershipId}")
  @Operation(
      summary = "Revoke membership",
      description = "Remove user access to an application")
  @ApiResponse(responseCode = "200", description = "Membership revoked")
  @ApiResponse(responseCode = "404", description = "Membership not found")
  public ResponseEntity<BaseResponse<Void>> revokeMembership(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Membership ID") @PathVariable UUID membershipId) {

    revokeMembershipUseCase.execute(MembershipId.of(membershipId), tenantSlug);

    BaseResponse<Void> response = BaseResponse.<Void>builder()
        .success(ResponseHelper.message(ResponseCode.MEMBERSHIP_REVOKED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }
}
