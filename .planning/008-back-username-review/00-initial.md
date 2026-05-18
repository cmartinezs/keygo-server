# 🌱 INITIAL: BACK-008 — Username Review

> **Status:** INITIAL
> [← planning/README.md](../../README.md)

---

## Intent

Auditar y normalizar el campo `username` en todo el sistema keygo: su definición, persistencia, generación automática, unicidad y exposición en la API — tanto para `PlatformUser` como para `TenantUser`.

---

## Why

El campo `username` tiene tratamientos asimétricos entre los dos modelos de usuario y entre capas. Los principales problemas detectados:

1. **`PlatformUser` no tiene columna `username` en BD** — se almacena como `display_name` en `platform_users` y se deriva en lectura vía `PlatformUserPersistenceMapper.resolveUsername()` (sanitizando chars → email prefix → UUID prefix). No hay constraint de unicidad a nivel de base de datos.

2. **`display_name` ≠ `username`** — para `PlatformUser` se usan como sinónimos en persistencia (`toEntity` guarda `username.value()` en `display_name`), lo que mezcla dos conceptos con semánticas distintas.

3. **Unicidad no garantizada en BD para PlatformUser** — el check de `existsByUsername` en `CreatePlatformUserUseCase` es solo a nivel de aplicación. Una condición de carrera puede insertar duplicados. `TenantUser` sí tiene índice único `(tenant_id, local_username)` en migración V6.

4. **Generación automática dispersa y heterogénea**:
   - `RegisterTenantUserUseCase.generateUsername()` — desde firstName + lastName
   - `PlatformUserPersistenceMapper.resolveUsername()` — desde display_name o email prefix
   - `PlatformUserController` — `email.split("@")[0]` directamente en el controller
   - `MockApprovePaymentUseCase` / `CreateAppContractUseCase` — desde `contract.generateUsername()`

5. **Validación inconsistente en API** — `CreateUserRequest` tiene `@Size(min=3, max=100)` pero no aplica el mismo regex del `Username` VO (`^[a-zA-Z0-9_.\\-]{3,100}$`). La validación en API y en dominio pueden aceptar/rechazar valores distintos.

6. **`local_username` en `TenantUser` es nullable** — el dominio exige `Username` no nulo en `User`, pero en BD el campo es opcional. El mapper `resolveUsername` en `UserPersistenceMapper` resuelve esto derivando el valor, lo que oculta si un usuario tiene username real o derivado.

---

## Approximate Scope

- `keygo-domain`: `Username` VO, `User`, `PlatformUser`
- `keygo-app`: `CreatePlatformUserUseCase`, `CreateUserUseCase`, `RegisterTenantUserUseCase`, ports `PlatformUserRepositoryPort`, `UserRepositoryPort`
- `keygo-supabase`: `PlatformUserEntity`, `TenantUserEntity`, `PlatformUserPersistenceMapper`, `UserPersistenceMapper`, migraciones
- `keygo-api`: `CreateUserRequest`, `RegisterRequest`, `UserData`, `UserProfileData`, `PlatformUserController`
- **Migración DB**: nueva columna `username` en `platform_users` + unique constraint

---

## Key Decisions Needed

- ¿`username` de PlatformUser debe ser: (a) columna propia en `platform_users`, (b) seguir derivado desde `display_name`, o (c) `display_name` separado de `username`?
- ¿Se mantiene `local_username` nullable para TenantUser o se hace obligatorio?
- ¿La generación automática de username debe centralizarse en un servicio de dominio?
- ¿Se agrega `@Pattern` en los DTOs de API para alinear con el VO?

---

## Initiator

- **Requested by:** Carlos Martínez
- **Date:** 2026-05-18
- **Related planning:** 003-back-p1-marcha-blanca (contexto de usuarios en marcha blanca)

---

## Next Step

- [ ] Obtener decisiones sobre los puntos en "Key Decisions Needed"
- [ ] Cuando dimensionado → fill `01-expansion.md` y mover a `planning/active/`

---

> [← planning/README.md](../../README.md)
