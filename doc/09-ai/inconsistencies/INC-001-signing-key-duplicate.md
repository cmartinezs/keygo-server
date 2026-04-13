# INC-001 — Dos inicializadores de signing key activos en perfil `supabase`

**Categoría:** configuración
**Criticidad:** 🟡 No crítica
**Estado:** 🔲 Pendiente
**Detectada:** 2026-03-25
**Resuelta:** —
**Tarea relacionada:** —

## Descripción

En el perfil `supabase`, dos clases inicializan la signing key al startup:
`SigningKeyBootstrapService` y `SigningKeyInitializer`. Ambas se instancian y ejecutan;
la segunda encuentra la clave ya generada por la primera y hace no-op.
Es inofensivo en runtime pero viola el principio de responsabilidad única en bootstrap
y confunde al lector del código.

## Esperado vs. Real

| | Esperado | Real |
|---|---|---|
| Inicializadores activos en `supabase` | Solo `SigningKeyInitializer` | `SigningKeyBootstrapService` + `SigningKeyInitializer` (ambos) |
| `SigningKeyBootstrapService` | Eliminado o desactivado | `@Profile("supabase")`, activo |
| `SigningKeyInitializer` | `@Profile({"supabase", "local"})` | ✅ Correcto tras fix de perfil `local` |

## Contexto

La causa original del bug fue que `SigningKeyInitializer` tenía solo `@Profile("supabase")`,
fallando en `local` con `No active signing key found`. El fix amplió su perfil a
`{"supabase", "local"}`, pero `SigningKeyBootstrapService` quedó activo y redundante.

**Archivos afectados:**
- `keygo-run/.../config/auth/SigningKeyBootstrapService.java` — candidato a eliminar
- `keygo-run/.../startup/SigningKeyInitializer.java` — correcto, conservar

## Corrección

Eliminar `SigningKeyBootstrapService`. Verificar que `SigningKeyInitializer` cubra
ambos perfiles correctamente antes de borrar.

```bash
# Verificar que no haya otras referencias antes de eliminar
grep -r "SigningKeyBootstrapService" keygo-run/src/
```
