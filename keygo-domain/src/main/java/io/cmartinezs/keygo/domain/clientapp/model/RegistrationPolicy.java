package io.cmartinezs.keygo.domain.clientapp.model;

/**
 * Enum that controls the behavior of self-registration for a ClientApp.
 *
 * <p>Enumeración que controla el comportamiento del auto-registro para una ClientApp.
 *
 * @author cmartinezs
 * @version 1.0
 */
public enum RegistrationPolicy {
  /**
   * Membership created with ACTIVE status upon email verification. User can access
   * immediately.
   *
   * <p>Membership creada con status ACTIVE al verificar email. El usuario puede acceder
   * inmediatamente.
   */
  OPEN_AUTO_ACTIVE,

  /**
   * Membership created with PENDING status upon email verification. Admin must approve
   * before access.
   *
   * <p>Membership creada con status PENDING al verificar email. Admin debe aprobar antes de
   * acceso.
   */
  OPEN_AUTO_PENDING,

  /**
   * No automatic membership creation. User is registered as a tenant user but not as a member
   * of this specific app. App access must be granted explicitly by an admin.
   *
   * <p>Sin creación automática de membership. El usuario queda registrado como tenant user pero
   * no como miembro de esta app. El acceso a la app debe ser otorgado explícitamente por un admin.
   */
  OPEN_NO_MEMBERSHIP,

  /**
   * Self-registration is disabled. Users can only access via invitation by an admin.
   *
   * <p>El auto-registro está deshabilitado. Los usuarios solo pueden acceder por invitación
   * de un admin.
   */
  INVITE_ONLY
}
