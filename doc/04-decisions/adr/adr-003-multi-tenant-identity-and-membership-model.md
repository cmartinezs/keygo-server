# ADR-003: Multi-Tenant Identity And Membership Model

- Fecha: 2026-04-11
- Estado: accepted

## Contexto

El dominio prioriza multi-tenancy, cuenta única por tenant, memberships por app y control explícito de roles y permisos.

## Decisión

Se documenta como canon el modelo IAM con estas piezas:

- Un `tenant` define el ámbito organizacional aislado.
- Un `tenant user` representa una identidad única dentro de un tenant.
- Una `membership` vincula a un `tenant user` con una `app` específica.
- Los roles y permisos pueden operar en ámbitos de plataforma, tenant y app según el caso de uso.

## Consecuencias

- La autorización debe evaluar ámbito, tenant y app; no basta con un rol global simple.
- La documentación funcional y de datos debe diferenciar claramente identidad, membresía y asignaciones de rol.
- Se evita modelar múltiples cuentas redundantes por app cuando el sujeto ya existe en el tenant.

## Alternativas consideradas

- Usuarios aislados por app sin `membership`.
- Un solo modelo de rol global para toda la plataforma.
