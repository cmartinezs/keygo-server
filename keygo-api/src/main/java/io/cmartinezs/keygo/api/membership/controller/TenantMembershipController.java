package io.cmartinezs.keygo.api.membership.controller;

import io.cmartinezs.keygo.api.membership.request.CreateMembershipRequest;
import io.cmartinezs.keygo.api.membership.response.MembershipData;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.api.shared.response.PagedData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.app.membership.command.CreateMembershipCommand;
import io.cmartinezs.keygo.app.membership.filter.MembershipFilter;
import io.cmartinezs.keygo.app.membership.usecase.CreateMembershipUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ApproveMembershipUseCase;
import io.cmartinezs.keygo.app.membership.usecase.ListMembershipsUseCase;
import io.cmartinezs.keygo.app.membership.usecase.RevokeMembershipUseCase;
import io.cmartinezs.keygo.app.shared.PagedResult;
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
@PreAuthorize("hasAnyRole('ADMIN','ADMIN_TENANT','KEYGO_ADMIN','KEYGO_TENANT_ADMIN') and @tenantAuthorizationEvaluator.hasTenantAccess(authentication)")
public class TenantMembershipController {

  private final CreateMembershipUseCase createMembershipUseCase;
  private final ApproveMembershipUseCase approveMembershipUseCase;
  private final ListMembershipsUseCase listMembershipsUseCase;
  private final RevokeMembershipUseCase revokeMembershipUseCase;

  public TenantMembershipController(
      CreateMembershipUseCase createMembershipUseCase,
      ApproveMembershipUseCase approveMembershipUseCase,
      ListMembershipsUseCase listMembershipsUseCase,
      RevokeMembershipUseCase revokeMembershipUseCase) {
    this.createMembershipUseCase = createMembershipUseCase;
    this.approveMembershipUseCase = approveMembershipUseCase;
    this.listMembershipsUseCase = listMembershipsUseCase;
    this.revokeMembershipUseCase = revokeMembershipUseCase;
  }

  @PostMapping
  @Operation(
      summary = "Create a membership",
      description = "Grant user access to an application with specified roles")
  @ApiResponse(responseCode = "201", description = "Membership created (code: MEMBERSHIP_CREATED)")
  @ApiResponse(responseCode = "400", description = "Request body validation failed (code: INVALID_INPUT). data.field_errors lists each invalid field.",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "403", description = "Tenant suspended or membership inactive (code: BUSINESS_RULE_VIOLATION)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Tenant or client app not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "Membership already exists for this user + app (code: DUPLICATE_RESOURCE)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
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
      description = "List memberships with optional pagination, filtering by user or app, and sorting")
  @ApiResponse(responseCode = "200", description = "Memberships retrieved (code: MEMBERSHIP_LIST_RETRIEVED)")
  @ApiResponse(responseCode = "400", description = "Invalid pagination parameters (code: INVALID_INPUT). data.field_errors lists each invalid field.",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<PagedData<MembershipData>>> listMemberships(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Filter by user ID") @RequestParam(name = "user_id", required = false) UUID userId,
      @Parameter(description = "Filter by client app ID") @RequestParam(name = "client_app_id", required = false) UUID clientAppId,
      @Parameter(description = "Zero-based page number", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Page size (1–200)", example = "20")
      @RequestParam(defaultValue = "20") int size,
      @Parameter(description = "Sort field (createdAt)")
      @RequestParam(required = false) String sort,
      @Parameter(description = "Sort order (ASC, DESC)", example = "ASC")
      @RequestParam(required = false) String order) {

    MembershipFilter filter = MembershipFilter.of(userId, clientAppId, page, size, sort, order);
    PagedResult<Membership> result = listMembershipsUseCase.execute(tenantSlug, filter);

    List<MembershipData> data = result.getContent().stream()
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

    PagedData<MembershipData> pagedData = PagedData.<MembershipData>builder()
        .content(data)
        .page(result.getPage())
        .size(result.getSize())
        .totalElements(result.getTotalElements())
        .totalPages(result.getTotalPages())
        .last(result.isLast())
        .build();

    BaseResponse<PagedData<MembershipData>> response = BaseResponse.<PagedData<MembershipData>>builder()
        .data(pagedData)
        .success(ResponseHelper.message(ResponseCode.MEMBERSHIP_LIST_RETRIEVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @PutMapping("/{membershipId}/approve")
  @Operation(
      summary = "Approve a pending membership",
      description = "Transitions a PENDING membership to ACTIVE status")
  @ApiResponse(responseCode = "200", description = "Membership approved (code: MEMBERSHIP_APPROVED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Membership not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "Membership already active or suspended (code: BUSINESS_RULE_VIOLATION)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  public ResponseEntity<BaseResponse<MembershipData>> approveMembership(
      @Parameter(description = "Tenant slug") @PathVariable String tenantSlug,
      @Parameter(description = "Membership ID") @PathVariable UUID membershipId) {

    var result = approveMembershipUseCase.execute(
        MembershipId.of(membershipId), tenantSlug);

    var membership = result.membership();
    MembershipData data = MembershipData.builder()
        .id(membership.getId().value())
        .userId(membership.getUserId().value())
        .clientAppId(membership.getClientAppId().value())
        .status(membership.getStatus())
        .roleIds(membership.getRoles().stream()
            .map(r -> r.roleId().value())
            .collect(java.util.stream.Collectors.toSet()))
        .notificationEmail(result.maskedEmail())
        .build();

    BaseResponse<MembershipData> response = BaseResponse.<MembershipData>builder()
        .data(data)
        .success(ResponseHelper.message(ResponseCode.MEMBERSHIP_APPROVED))
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @DeleteMapping("/{membershipId}")
  @Operation(
      summary = "Revoke membership",
      description = "Remove user access to an application")
  @ApiResponse(responseCode = "200", description = "Membership revoked (code: MEMBERSHIP_REVOKED)")
  @ApiResponse(responseCode = "401", description = "Missing or invalid Bearer token (code: AUTHENTICATION_REQUIRED)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
  @ApiResponse(responseCode = "404", description = "Membership not found (code: RESOURCE_NOT_FOUND)",
      content = @Content(schema = @Schema(implementation = BaseResponse.ErrorResponse.class)))
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
