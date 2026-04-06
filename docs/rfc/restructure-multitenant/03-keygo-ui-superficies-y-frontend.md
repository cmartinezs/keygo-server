# 03. Modelo funcional de `keygo-ui` y organización del frontend

## Objetivo

Definir qué es realmente `keygo-ui` dentro del producto y cómo conviene organizarlo sin confundir funciones distintas bajo una sola interfaz.

---

## 1. Problema a resolver

Actualmente la landing y la aplicación viven dentro del mismo proyecto React + Vite. Eso es perfectamente viable en una etapa inicial, pero exige una separación conceptual clara.

El riesgo no es que todo esté en un solo proyecto.  
El riesgo es tratar como una sola cosa:

- la landing de marketing,
- el login hosted,
- el portal de cuenta del usuario,
- la console de administración del tenant,
- y las herramientas de operación de plataforma.

Cada una responde a objetivos, permisos, layouts, guardas y API distintas.

---

## 2. Postura recomendada

Se recomienda mantener, por ahora:

- **un solo proyecto frontend físico**,  
pero modelado como  
- **varias superficies lógicas bien separadas**.

---

## 3. Superficies lógicas recomendadas

## 3.1 Landing pública

### Propósito
Presentación comercial, onboarding inicial, pricing, branding y captación.

### Características
- acceso público,
- orientación SEO,
- mínima dependencia de sesión,
- componentes livianos,
- contenido informativo y comercial.

### Ejemplos de rutas
- `/`
- `/pricing`
- `/about`
- `/docs`
- `/contact`

---

## 3.2 Hosted Auth

### Propósito
Resolver los flujos de autenticación y recuperación de acceso.

### Incluye
- login,
- registro,
- forgot password,
- reset password,
- verify email,
- eventualmente MFA y selección de organización.

### Ejemplos de rutas
- `/auth/login`
- `/auth/register`
- `/auth/forgot-password`
- `/auth/reset-password`
- `/auth/verify-email`

---

## 3.3 Portal de cuenta personal

### Propósito
Permitir que la persona gestione su cuenta global dentro de Keygo.

### Incluye
- perfil,
- credenciales,
- sesiones,
- actividad,
- seguridad,
- dispositivos, si se incorpora más adelante.

### Observación importante
Esta superficie depende de la **cuenta global de plataforma**, no de pertenecer al tenant interno `keygo`.

### Ejemplos de rutas
- `/me/profile`
- `/me/security`
- `/me/sessions`
- `/me/activity`

---

## 3.4 Console de tenant

### Propósito
Administrar el tenant del cliente.

### Incluye
- dashboard del tenant,
- apps,
- usuarios,
- memberships,
- billing,
- configuración.

### Observación importante
Esta superficie es **tenant-aware**: requiere contexto de tenant y permisos específicos.

### Ejemplos de rutas
- `/console`
- `/console/apps`
- `/console/users`
- `/console/billing`
- `/console/settings`

---

## 3.5 Ops de plataforma

### Propósito
Operar Keygo como servicio.

### Incluye
- administración global de tenants,
- soporte,
- auditoría global,
- settings operacionales,
- observabilidad administrativa.

### Ejemplos de rutas
- `/ops/tenants`
- `/ops/users`
- `/ops/audit`

---

## 4. Qué significa esto en la práctica

Aunque hoy exista una sola SPA, se recomienda tratarla como si internamente fueran cinco frontends lógicos.

Esto implica separar:

- rutas,
- layouts,
- guards,
- módulos,
- API clients,
- navegación,
- y reglas de autorización.

---

## 5. Organización recomendada del proyecto React + Vite

Una estructura posible es la siguiente:

```text
src/
  app/
    router/
    layouts/
    guards/
  features/
    public/
      landing/
      pricing/
      docs/
    auth/
      login/
      register/
      forgot-password/
      reset-password/
    account/
      profile/
      security/
      sessions/
      activity/
    console/
      dashboard/
      apps/
      users/
      billing/
      settings/
    ops/
      tenants/
      support/
      audit/
  shared/
    ui/
    api/
    hooks/
    utils/
    lib/
```

---

## 6. Layouts recomendados

Se recomienda definir layouts distintos por superficie:

- `PublicLayout`
- `AuthLayout`
- `AccountLayout`
- `ConsoleLayout`
- `OpsLayout`

Esto reduce el acoplamiento visual y funcional.

---

## 7. Guards recomendados

Al menos conviene modelar estos guards:

- `RequireAuthenticated`
- `RequireAccountAccess`
- `RequireTenantContext`
- `RequireTenantAdmin`
- `RequirePlatformAdmin`

El objetivo es que la navegación no dependa de lógica suelta repartida en componentes.

---

## 8. Relación entre `keygo-ui` y las apps cliente

Es importante formular bien este punto:

La app cliente **no usa `keygo-ui` como su UI principal**.  
Lo que hace es **delegar autenticación en Keygo**.

Eso significa que la app cliente usa:

- el hosted login de Keygo,
- la sesión global de Keygo para SSO,
- y sus propios tokens para operar dentro de su contexto.

En otras palabras:

- `keygo-ui` aporta la experiencia de autenticación,
- pero no reemplaza la UI de negocio de la app cliente.

---

## 9. Sobre mantener landing + app en el mismo proyecto

## Recomendación actual
Sí es válido mantenerlas juntas por ahora.

## Condición
Deben estar desacopladas a nivel de:

- carpetas,
- rutas,
- carga,
- layouts,
- y navegación.

## Evolución sugerida
Más adelante, si el producto lo requiere, se puede separar el despliegue de:

- `www.keygo.cl` para la landing,
- `app.keygo.cl` para auth, account y console,
- y eventualmente un dominio o subdominio de ops.

---

## 10. Conclusión

La mejor forma de entender `keygo-ui` no es como “una sola pantalla grande”, sino como una **superficie web compuesta** con cinco áreas diferenciadas.

La decisión recomendada es:

- mantener un solo frontend por ahora,
- pero modelarlo como varios subproductos lógicos,
- con responsabilidades, permisos y rutas separadas.
