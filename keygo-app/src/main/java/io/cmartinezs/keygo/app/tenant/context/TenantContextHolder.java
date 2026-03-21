package io.cmartinezs.keygo.app.tenant.context;

/**
 * Thread-local holder for the current request's resolved tenant slug.
 * Set by TenantResolutionFilter at the beginning of each request and cleared at the end.
 * <p>Contenedor ThreadLocal del slug de tenant resuelto para la request actual.
 * Establecido por TenantResolutionFilter al inicio de cada request y limpiado al finalizar.
 * @author cmartinezs
 * @version 1.0
 */
public final class TenantContextHolder {

  private static final ThreadLocal<String> CURRENT_TENANT_SLUG = new ThreadLocal<>();

  private TenantContextHolder() {
    // Utility class — no instantiation
  }

  /**
   * Set the current tenant slug for the executing thread.
   * <p>Establece el slug del tenant actual para el hilo en ejecución.
   * @param slug the resolved tenant slug
   */
  public static void set(String slug) {
    CURRENT_TENANT_SLUG.set(slug);
  }

  /**
   * Get the current tenant slug for the executing thread.
   * <p>Obtiene el slug del tenant actual para el hilo en ejecución.
   * @return the tenant slug, or {@code null} if not set
   */
  public static String get() {
    return CURRENT_TENANT_SLUG.get();
  }

  /**
   * Returns true if a tenant slug is set for the current thread.
   * <p>Retorna true si hay un slug de tenant establecido para el hilo actual.
   */
  public static boolean isPresent() {
    return CURRENT_TENANT_SLUG.get() != null;
  }

  /**
   * Clear the current tenant slug to avoid memory leaks.
   * <p>Limpia el slug del tenant actual para evitar memory leaks.
   */
  public static void clear() {
    CURRENT_TENANT_SLUG.remove();
  }
}

