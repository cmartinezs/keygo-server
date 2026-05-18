# Guidelines para AI Agent — Implementación de marcha blanca KeyGo

| Campo | Valor |
|---|---|
| Tipo | Cross-cutting / Instrucciones generales |
| Prioridad | Obligatoria |
| Área | Frontend y Backend |
| Objetivo | Establecer reglas de implementación seguras para aplicar las specifications del paquete |

## Contexto

KeyGo está cerca de una marcha blanca controlada, pero existen brechas de contrato entre UI, endpoints, DTO/modelo y experiencia funcional. El objetivo de estas specifications es cerrar los bloqueantes mínimos para usar KeyGo con un tenant piloto, pocas apps y pocos usuarios.

## Reglas generales para el AI Agent

1. No mezclar cambios de Frontend y Backend en un mismo commit lógico si el repositorio está separado.
2. No cambiar nombres públicos de endpoints sin actualizar contratos, clientes API, mocks, tests y documentación.
3. No introducir comportamiento autoservicio público si la specification indica mantener el piloto cerrado.
4. No dejar mocks activos en flujos considerados centrales para marcha blanca.
5. No inventar permisos, roles ni claims distintos a los definidos en estas specifications sin documentar explícitamente la decisión.
6. Mantener compatibilidad con la decisión base del producto: **identidad única por tenant y acceso por membership por app**.
7. Cada cambio debe incluir pruebas mínimas de regresión o, si no es posible, una sección `TODO técnico explícito` en el PR.

## Convenciones funcionales

### Tenant

Un tenant representa una organización cliente. Los usuarios pertenecen al tenant, no directamente a una app.

### Client App

Una app cliente pertenece a un tenant. Puede ser `PUBLIC` o `CONFIDENTIAL`. Debe configurar grants, scopes y redirect URIs cuando corresponda.

### Membership

Una membership representa la asignación de un usuario del tenant a una app del mismo tenant. Es el eje de autorización por app.

### Roles

- Roles administrativos de KeyGo: controlan acceso a consola KeyGo.
- Roles de app: controlan permisos del usuario dentro de una app cliente.

## Formato esperado de trabajo del AI Agent

Para cada specification:

1. Leer el archivo completo.
2. Identificar archivos existentes en el repositorio.
3. Proponer cambios mínimos.
4. Implementar.
5. Agregar o actualizar tests.
6. Actualizar documentación local si corresponde.
7. Reportar:
   - archivos modificados,
   - decisiones tomadas,
   - criterios de aceptación cubiertos,
   - criterios pendientes.

## Restricciones de marcha blanca

No habilitar todavía:

- Autoservicio público completo.
- Auto-join generalizado.
- Solicitud de acceso abierta sin flujo de aprobación terminado.
- Billing real.
- Administración masiva de usuarios.
- Tenants externos autogestionados.

## Definition of Done global

Una specification se considera terminada cuando:

- La funcionalidad compila y pasa tests existentes.
- Los contratos UI ↔ API quedan alineados.
- No quedan mocks ocultando la funcionalidad principal.
- El comportamiento queda cubierto por pruebas o validación manual documentada.
- La UI no permite ejecutar acciones que el backend todavía no soporta.
