# 09. Roadmap sugerido

## Objetivo

Traducir las definiciones de esta propuesta en una secuencia de implementación razonable para que la mejora pueda presentarse también como plan de trabajo.

---

## Fase 1 — Alineamiento conceptual y documental

### Objetivo
Cerrar la semántica del sistema antes de seguir ampliando funcionalidad.

### Entregables
- documentación base de identidad/tenant/app,
- documentación del rol de `keygo-ui`,
- naming oficial de roles de Keygo,
- criterio oficial para JWT.

### Resultado esperado
Que producto, backend y frontend hablen el mismo idioma.

---

## Fase 2 — Reordenamiento del frontend

### Objetivo
Separar el proyecto actual React + Vite por superficies.

### Entregables
- módulos `public`, `auth`, `account`, `console`, `ops`,
- layouts diferenciados,
- guards centralizados,
- contexto de sesión y tenant más explícito.

### Resultado esperado
Mejor mantenibilidad y menor confusión entre áreas del sistema.

---

## Fase 3 — Consolidación de autenticación

### Objetivo
Dejar claro el patrón hosted login + sesión global + sesión de app.

### Entregables
- flujo `/authorize` bien definido,
- hosted login unificado,
- política de sesión global,
- diferenciación de logout local y logout global.

### Resultado esperado
SSO más consistente y menos ambigüedad técnica.

---

## Fase 4 — Formalización del RBAC de Keygo

### Objetivo
Separar la autorización del producto respecto del RBAC de tenant y app.

### Entregables
- roles de Keygo oficialmente definidos,
- herencia o jerarquía acordada,
- evaluación de autorización centralizada,
- navegación del frontend alineada a capacidades.

### Resultado esperado
Control de acceso más limpio y menos lógica dispersa.

---

## Fase 5 — Endpoints de identidad y autorización efectiva

### Objetivo
Mejorar ergonomía del frontend sin inflar el JWT.

### Entregables
- `GET /me`
- `GET /me/authorization`
- capacidades efectivas resumidas
- contexto actual de usuario y tenant

### Resultado esperado
Frontend más simple y backend más coherente.

---

## Fase 6 — Evoluciones posteriores

### Posibles siguientes pasos
- MFA,
- dispositivos,
- auditoría ampliada,
- separación de despliegue entre landing y app,
- más configuraciones por app,
- más control fino por permisos.

---

## Priorización recomendada

### Alta prioridad
- fijar modelo conceptual,
- ordenar frontend,
- separar sesión global y sesión de app,
- clarificar RBAC de Keygo.

### Media prioridad
- endpoint de autorización efectiva,
- capacidades resumidas para frontend,
- políticas de acceso por app configurables.

### Baja prioridad inicial
- separación física de landing y app,
- permisos hiper granulares,
- personalización muy avanzada.

---

## Mensaje final de roadmap

La propuesta no exige rehacer el producto desde cero.  
Lo que propone es introducir orden estructural en los puntos que, si se dejan ambiguos, tienden a multiplicar deuda técnica y conceptual.

La secuencia recomendada es:

1. aclarar,
2. ordenar,
3. consolidar,
4. evolucionar.
