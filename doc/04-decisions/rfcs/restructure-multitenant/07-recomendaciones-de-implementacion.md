# 07. Recomendaciones de implementación

## Objetivo

Traducir las decisiones conceptuales a un conjunto de recomendaciones concretas y priorizadas para implementación.

---

## 1. Recomendaciones de corto plazo

## 1.1 Consolidar el modelo conceptual
Antes de seguir agregando pantallas o endpoints, conviene fijar explícitamente estas definiciones en la documentación base del proyecto:

- cuenta global de plataforma,
- usuario por tenant,
- membership por app,
- tenant interno `keygo` como tenant reservado, no universal,
- RBAC propio de Keygo separado del resto.

### Resultado esperado
Evitar retrabajo y contradicciones posteriores.

---

## 1.2 Ordenar el frontend por superficies
Dentro del proyecto React + Vite actual, separar en módulos:

- public,
- auth,
- account,
- console,
- ops.

### Resultado esperado
Reducir acoplamiento y preparar el sistema para un crecimiento más limpio.

---

## 1.3 Centralizar guards y contexto
Definir explícitamente:

- contexto de sesión global,
- tenant actual,
- rol/capacidad efectiva,
- guards por superficie.

### Resultado esperado
Evitar lógica de seguridad dispersa en componentes.

---

## 1.4 Unificar criterios de naming
Adoptar nombres consistentes para:

- `platform_user`,
- `tenant_user`,
- `app_membership`,
- `KEYGO_ADMIN`,
- `KEYGO_ACCOUNT_ADMIN`,
- `KEYGO_USER`.

### Resultado esperado
Reducir ambigüedades en código, documentación y conversaciones futuras.

---

## 2. Recomendaciones de mediano plazo

## 2.1 Formalizar RBAC de Keygo
Partir simple si hace falta, pero dejar un punto único de evaluación.

### Mínimo recomendable
- roles claros,
- herencia clara,
- autorización centralizada.

### Evolución recomendable
- capacidades,
- permisos,
- matriz declarativa.

---

## 2.2 Diseñar sesiones con dos niveles
Implementar explícitamente:

- sesión global de Keygo,
- sesión de app cliente.

### Resultado esperado
Tener un SSO creíble y evitar problemas futuros de logout y trazabilidad.

---

## 2.3 Definir un endpoint de identidad/autorización efectiva
Por ejemplo:
- `GET /me`
- `GET /me/authorization`

### Posible contenido
- identidad básica,
- tenant actual,
- roles,
- capacidades resumidas,
- estado de sesión.

### Resultado esperado
Facilitar el frontend sin inflar el JWT.

---

## 2.4 Formalizar políticas de acceso por app
Incorporar explícitamente una configuración por app, por ejemplo:

- `CLOSED`
- `OPEN_JOIN`
- `SELF_SIGNUP`

### Resultado esperado
Tener un modelo consistente para invitación, onboarding y control de acceso.

---

## 3. Recomendaciones de largo plazo

## 3.1 Separación de despliegue del frontend
Evaluar separar:

- landing pública,
- app de autenticación y administración,
- y eventualmente ops.

No es obligatorio hoy, pero sí conviene dejarlo preparado.

---

## 3.2 Evolución del modelo de seguridad
A futuro puede ser razonable incorporar:

- MFA,
- gestión de dispositivos,
- reautenticación por operación sensible,
- auditoría más rica,
- consentimiento,
- y eventos de seguridad.

---

## 3.3 Evolución del modelo de identidad
Si el producto madura lo suficiente, se puede profundizar la noción de cuenta global y sus vínculos con tenants, sin modificar el principio base:

- identidad global,
- pertenencia por tenant,
- acceso por app.

---

## 4. Riesgos que esta propuesta busca evitar

### Riesgo 1
Que el tenant `keygo` termine usado para todo y pierda su significado organizacional.

### Riesgo 2
Que `keygo-ui` se convierta en una mezcla difícil de mantener entre sitio público, login, portal de usuario y console.

### Riesgo 3
Que los roles del producto terminen mezclados con los roles de negocio de tenants y apps.

### Riesgo 4
Que el JWT crezca demasiado y termine siendo la fuente de verdad equivocada para autorización.

### Riesgo 5
Que SSO, login hosted y sesión de app se traten como una sola cosa.

---

## 5. Definición de “done” a nivel de arquitectura

Se puede considerar que esta mejora está correctamente adoptada cuando:

- existe documentación base consistente,
- el frontend está separado por superficies,
- el RBAC de Keygo está identificado como ámbito propio,
- los tokens tienen semántica clara y acotada,
- y el modelo de identidad/tenant/app se refleja en nombres, flujos y endpoints.

---

## 6. Cierre

La mejor implementación no es necesariamente la más sofisticada, sino la que deja una base consistente para crecer.

Por eso la recomendación general de este paquete es:

- empezar con estructuras simples,
- pero con límites conceptuales correctos,
- para no pagar después el costo de decisiones ambiguas.
