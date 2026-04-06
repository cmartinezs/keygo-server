# Keygo — Documentación de mejoras funcionales y técnicas

## Propósito

Este paquete consolida una propuesta de mejora para **Keygo** a nivel de producto, modelo de dominio, autenticación, autorización y organización de frontend, tomando como base las definiciones revisadas para el sistema IAM multitenant y aterrizándolas como documentación de proyecto.

La intención de estos documentos es que puedan ser usados como:

- material de presentación interna,
- respaldo de decisiones arquitectónicas,
- guía de implementación,
- base para backlog técnico y funcional.

## Alcance de esta propuesta

Esta documentación cubre principalmente:

- modelo de identidad y multitenancy,
- relación entre cuenta global, tenant, usuario de tenant y acceso a apps,
- rol de `keygo-ui` dentro del ecosistema,
- hosted login y reutilización de sesión global,
- separación de responsabilidades entre cuenta, console, ops y login,
- RBAC específico de Keygo,
- estrategia recomendada para JWT y autorización.

## Índice

### 0. Contexto y objetivo
- [00-contexto-y-objetivo.md](./00-contexto-y-objetivo.md)

### 1. Decisiones estructurales del producto
- [01-decisiones-estructurales.md](./01-decisiones-estructurales.md)

### 2. Modelo de identidad, tenant y aplicaciones
- [02-modelo-identidad-multitenancy.md](./02-modelo-identidad-multitenancy.md)

### 3. Modelo funcional de `keygo-ui`
- [03-keygo-ui-superficies-y-frontend.md](./03-keygo-ui-superficies-y-frontend.md)

### 4. Hosted login, SSO y sesiones
- [04-hosted-login-sso-y-sesiones.md](./04-hosted-login-sso-y-sesiones.md)

### 5. RBAC de Keygo
- [05-rbac-de-keygo.md](./05-rbac-de-keygo.md)

### 6. JWT y estrategia de autorización
- [06-jwt-y-autorizacion.md](./06-jwt-y-autorizacion.md)

### 7. Recomendaciones de implementación
- [07-recomendaciones-de-implementacion.md](./07-recomendaciones-de-implementacion.md)

### 8. Diagramas Mermaid
- [08-diagramas-mermaid.md](./08-diagramas-mermaid.md)

### 9. Roadmap sugerido
- [09-roadmap-sugerido.md](./09-roadmap-sugerido.md)

### 10. Estado de implementación actual
- [10-estado-implementacion-actual.md](./10-estado-implementacion-actual.md)

## Orden de lectura sugerido

Si se requiere una lectura rápida para presentación:

1. `00-contexto-y-objetivo.md`
2. `01-decisiones-estructurales.md`
3. `03-keygo-ui-superficies-y-frontend.md`
4. `05-rbac-de-keygo.md`
5. `06-jwt-y-autorizacion.md`
6. `09-roadmap-sugerido.md`

Si se requiere una lectura más técnica:

1. `02-modelo-identidad-multitenancy.md`
2. `04-hosted-login-sso-y-sesiones.md`
3. `05-rbac-de-keygo.md`
4. `06-jwt-y-autorizacion.md`
5. `07-recomendaciones-de-implementacion.md`
6. `08-diagramas-mermaid.md`
7. `10-estado-implementacion-actual.md` (estado vs. RFC)

## Resultado esperado

Al adoptar esta propuesta, Keygo debería quedar mejor posicionado para:

- evitar ambigüedades entre cuenta global y pertenencia a tenant,
- crecer desde un MVP usable hacia un IAM SaaS más sólido,
- soportar hosted login sin exigir login propio en las apps cliente,
- separar correctamente responsabilidades de frontend,
- simplificar el manejo de roles, permisos y tokens,
- preparar el sistema para evolución futura sin rehacer la base conceptual.
