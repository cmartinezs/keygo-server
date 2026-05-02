package io.cmartinezs.keygo.domain.user.model;

/**
 * Status of a user account within a tenant.
 * <p>Estado de una cuenta de usuario dentro de un tenant.
 * @author cmartinezs
 * @version 1.0
 */
public enum UserStatus {

  /* User was invited to the tenant but has not completed activation. */
  INVITED,

  /* User account is active and can authenticate.
   * La cuenta de usuario está activa y puede autenticarse. */
  ACTIVE,

  /* User account is suspended and cannot authenticate.
   * La cuenta de usuario está suspendida y no puede autenticarse. */
  SUSPENDED,

  /* User account is pending activation (e.g. awaiting email verification).
   * La cuenta está pendiente de activación (ej. esperando verificación de email). */
  PENDING,

  /* User was provisioned with a temporary password and must reset it before gaining full access.
   * El usuario fue aprovisionado con una contraseña temporal y debe cambiarla antes de tener acceso completo. */
  RESET_PASSWORD,

  /* User was deleted from the tenant scope. */
  DELETED
}

