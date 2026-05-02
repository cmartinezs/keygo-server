---
mode: agent
---

# Agregar entidad JPA + repository en keygo-supabase

Objetivo: agregar una nueva entidad JPA y su Spring Data repository en el módulo `keygo-supabase`.

## Reglas

- Usar UUID como clave primaria (`@GeneratedValue` con `UUID`).
- Incluir campos de auditoría: `createdAt`, `updatedAt` (con `@CreationTimestamp` / `@UpdateTimestamp`).
- Naming de tabla: `snake_case`, consistente con las entidades existentes.
- Agregar índices donde tenga sentido (campos de búsqueda frecuente).
- El `Repository` interface extiende `JpaRepository` con métodos mínimos y claros.
- **No hardcodear credenciales** en ningún archivo.
- Si se requieren migraciones Flyway: proponer el script SQL con nombre correcto (`V<n>__descripcion.sql`) sin ejecutarlo en producción directamente.

## Entrega esperada

- Código de la entidad JPA.
- Código del repository interface.
- Recomendación de migración Flyway (si aplica).
- Tests unitarios básicos (si aplica).
- Comandos de verificación:

```bash
./mvnw -pl keygo-supabase test
./mvnw clean package
```

