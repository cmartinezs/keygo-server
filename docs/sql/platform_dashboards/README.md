# Platform Dashboards SQL

Consultas SQL listas para backend, UI y reporting sobre el baseline remade de Keygo.

## Propósito

- Mantener separadas las consultas analíticas del DDL de Flyway.
- Organizar el acceso por perfil de plataforma.
- Garantizar que toda métrica agregada importante tenga su consulta de drill-down.

## Estructura

```text
docs/sql/platform_dashboards/
  README.md
  common/
  keygo_admin/
  keygo_account_admin/
  keygo_user/
```

## Convención de nombres

- Queries agregadas: `*_count.sql`, `*_summary.sql`, `*_by_day.sql`, `top_*`
- Queries detalle: `*_detail.sql`, `list_*`
- Si una query agregada tiene varias métricas relacionadas, el detalle asociado se indica en el comentario inicial del archivo.

## Parámetros estándar

- `:from_ts`
- `:to_ts`
- `:tenant_id`
- `:contractor_id`
- `:platform_user_id`
- `:client_app_id`
- `:limit`
- `:offset`

## Regla Aggregate + Detail

- Cada agregado relevante en `keygo_admin/` tiene un archivo detalle asociado.
- Cada agregado relevante en `keygo_account_admin/` tiene un archivo detalle asociado.
- En `keygo_user/`, los resúmenes agregados también se acompañan de detalle; las consultas puramente self-service ya son detalle por sí mismas.

## Cobertura por perfil

- `keygo_admin/`: métricas globales de plataforma, seguridad, billing y actividad.
- `keygo_account_admin/`: visión acotada a contractor y tenants administrados.
- `keygo_user/`: self-service global sobre cuenta, sesiones, actividad y organizaciones.
