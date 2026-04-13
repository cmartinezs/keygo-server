# Diccionario de tipos PostgreSQL ↔ JPA/Java

Referencia operativa para decidir tipos físicos en migraciones Flyway y su mapeo canonico
en JPA/Java dentro de `keygo-supabase`.

## Fuente de verdad

1. El tipo fisico lo define primero el DDL activo en `keygo-supabase/src/main/resources/db/migration/`.
2. JPA debe mapearse al tipo fisico real; no al tipo "parecido" mas comodo.
3. Si el dominio necesita semantica distinta, se resuelve con conversion explicita en mappers,
   no dejando que Hibernate adivine.

## Tabla de referencia

| PostgreSQL | JPA/Java canonico | Regla de uso | Notas |
|---|---|---|---|
| `UUID` | `UUID` | Identificadores tecnicos y FKs | `@GeneratedValue(strategy = GenerationType.UUID)` cuando aplique |
| `VARCHAR(n)` | `String` | Texto acotado con longitud funcional conocida | Declarar `length = n` en `@Column` |
| `TEXT` | `String` | Texto libre o snapshots largos | Usar `columnDefinition = "TEXT"` si se necesita alinear validacion de esquema |
| `CITEXT` | `String` | Emails u otros textos case-insensitive persistidos en PostgreSQL | Declarar `columnDefinition = "citext"` |
| `BOOLEAN` | `boolean` / `Boolean` | Flags | `Boolean` solo si el valor puede ser nulo |
| `INTEGER` | `int` / `Integer` | Valores enteros pequenos | `Integer` si acepta `NULL` |
| `BIGINT` | `long` / `Long` | Contadores o enteros grandes sin decimales | No usar para columnas `NUMERIC` |
| `NUMERIC(p,s)` | `BigDecimal` | Dinero, porcentajes, cantidades con precision fija o valores que pueden evolucionar a decimales | Declarar `precision` y `scale` en `@Column` |
| `DATE` | `LocalDate` | Fechas sin zona horaria | Evitar `OffsetDateTime` aqui |
| `TIMESTAMPTZ` | `OffsetDateTime` | Fechas/hora auditables con offset | Canonico para `created_at`, `updated_at`, expiraciones y eventos |
| `JSONB` | `String` o tipo JSON explicito | Payloads, metadata y snapshots estructurados | Usar `@JdbcTypeCode(SqlTypes.JSON)` + `columnDefinition = "jsonb"` |

## Reglas de decision

### 1. `NUMERIC` siempre mapea a `BigDecimal`

Aunque los ejemplos actuales usen enteros (`10`, `25`, `0`), una columna `NUMERIC` se modela
en Java como `BigDecimal`.

Esto aplica especialmente a:

- montos monetarios
- porcentajes
- limites de entitlements
- contadores persistidos como `NUMERIC`

Si el negocio exige enteros estrictos, la correccion debe hacerse en el **DDL** (`INTEGER` o
`BIGINT`), no modelando `NUMERIC` como `Long`.

### 2. `CITEXT` requiere `columnDefinition = "citext"`

Para emails o valores case-insensitive:

- PostgreSQL debe persistir `CITEXT`
- JPA sigue usando `String`
- la entidad debe declarar `columnDefinition = "citext"` para evitar drift en schema validation

### 3. `JSONB` no se deja como `VARCHAR`

Cuando la columna fisica es `JSONB`, la entidad debe declararla explicitamente como tal:

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "metadata", columnDefinition = "jsonb")
private String metadata;
```

El tipo Java puede ser `String`, `Map<String, Object>` o `JsonNode`, pero debe mantenerse
coherente dentro de cada agregado.

### 4. El dominio puede ser mas estricto que persistencia, nunca mas laxo

Ejemplo:

- persistencia `NUMERIC(18,4)` → JPA `BigDecimal`
- dominio puede decidir tratar cierto caso como entero, pero con conversion validada
- no se debe dejar JPA en `Long` esperando que PostgreSQL convierta solo

## Criterios practicos

| Caso | Tipo SQL recomendado | Tipo Java recomendado |
|---|---|---|
| Email case-insensitive | `CITEXT` | `String` |
| Monto monetario | `NUMERIC(12,2)` | `BigDecimal` |
| Porcentaje | `NUMERIC(5,2)` | `BigDecimal` |
| Limite entero estricto | `INTEGER` / `BIGINT` | `Integer` / `Long` |
| Limite con posible precision futura | `NUMERIC(18,4)` | `BigDecimal` |
| Contador solo entero | `BIGINT` | `long` / `Long` |
| Metadata JSON | `JSONB` | `String` o estructura JSON explicita |
| Timestamp de auditoria | `TIMESTAMPTZ` | `OffsetDateTime` |

## Convencion del proyecto

- La compatibilidad se evalua contra las migraciones activas, no contra snapshots archivados.
- Si aparece una duda entre dos tipos posibles, primero se decide la semantica de negocio en el
  DDL y luego se alinea JPA.
- Ante drift detectado entre SQL y entidad, se corrige el mapeo o la migracion; no se aceptan
  "equivalencias aproximadas".
