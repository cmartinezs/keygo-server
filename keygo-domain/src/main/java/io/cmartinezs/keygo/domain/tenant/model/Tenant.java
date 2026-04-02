package io.cmartinezs.keygo.domain.tenant.model;

import io.cmartinezs.keygo.domain.tenant.exception.TenantSuspendedException;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/**
 * Tenant domain entity — the top-level isolation boundary of the system.
 * Every resource (users, client apps, memberships) belongs to a tenant.
 * <p>Entidad de dominio Tenant — la frontera de aislamiento de nivel superior del sistema.
 * Cada recurso (usuarios, apps cliente, membresías) pertenece a un tenant.
 * @author cmartinezs
 * @version 1.0
 */
@Getter
public class Tenant {

  private final TenantId id;
  private final TenantSlug slug;
  private final String name;
  private final String ownerEmail;
  private TenantStatus status;
  /**
   * NULL = system/platform tenant. NOT NULL = created by a contractor.
   * Set when contractor activates a contract and creates this tenant.
   */
  private final UUID contractorId;

  @Builder
  private Tenant(
      TenantId id,
      TenantSlug slug,
      String name,
      String ownerEmail,
      TenantStatus status,
      UUID contractorId) {
    if (id == null) throw new IllegalArgumentException("Tenant id cannot be null");
    if (slug == null) throw new IllegalArgumentException("Tenant slug cannot be null");
    if (name == null || name.isBlank()) throw new IllegalArgumentException("Tenant name cannot be null or blank");
    if (ownerEmail == null || ownerEmail.isBlank()) throw new IllegalArgumentException("Tenant ownerEmail cannot be null or blank");
    if (status == null) throw new IllegalArgumentException("Tenant status cannot be null");

    this.id = id;
    this.slug = slug;
    this.name = name;
    this.ownerEmail = ownerEmail;
    this.status = status;
    this.contractorId = contractorId;
  }

  // ─── Domain behaviour ─────────────────────────────────────────────────────

  /**
   * Returns true if the tenant is active and operational.
   */
  public boolean isActive() {
    return TenantStatus.ACTIVE.equals(this.status);
  }

  /**
   * Returns true if the tenant is suspended.
   */
  public boolean isSuspended() {
    return TenantStatus.SUSPENDED.equals(this.status);
  }

  /**
   * Suspend this tenant. Throws if it is already suspended.
   * <p>Suspende este tenant. Lanza excepción si ya estaba suspendido.
   * @throws IllegalStateException if the tenant is already suspended
   */
  public void suspend() {
    if (TenantStatus.SUSPENDED.equals(this.status)) {
      throw new TenantSuspendedException(slug.value());
    }
    this.status = TenantStatus.SUSPENDED;
  }

  /**
   * Reactivate a previously suspended tenant.
   * <p>Reactiva un tenant previamente suspendido.
   */
  public void activate() {
    this.status = TenantStatus.ACTIVE;
  }

  @Override
  public String toString() {
    return "Tenant{id=" + id + ", slug=" + slug + ", status=" + status + "}";
  }
}

