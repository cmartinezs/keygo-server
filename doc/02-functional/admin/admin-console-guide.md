# Admin Console Guide

Guia funcional para superficies de administración de KeyGo.

## Ambitos

- `platform admin`: administra tenants, apps, usuarios de plataforma, claves y metricas globales.
- `tenant admin`: administra usuarios, memberships, roles y apps dentro de su tenant.

## Capacidades prioritarias

1. Gestion de tenants y estado operativo.
2. Gestion de `platform users`, `tenant users` y `memberships` por app.
3. Gestion de roles y permisos por ámbito.
4. Observacion de flujos OAuth2/OIDC, sesiones y claves de firma.
5. Soporte a billing y contratacion cuando aplica.

## Referencias cruzadas

- Arquitectura multi-tenant: [architecture.md](../../03-architecture/architecture.md)
- Autorización: [authorization-patterns.md](../../03-architecture/authorization-patterns.md)
- Dashboard histórico: [admin-dashboard.md](../../99-archive/research/admin-dashboard.md)

## Pendientes documentados

- Existe tension entre los dashboards hoy implementados y la superficie ideal de consola admin. Esa tension se mantiene como referencia en los RFCs y en el research archivado; no se redefine aqui como decisión cerrada.

