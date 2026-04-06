# 04. Hosted login, SSO y manejo de sesiones

## Objetivo

Definir cómo debe funcionar el login hosted de Keygo y cómo separar correctamente la sesión global del sistema de la sesión propia de cada app cliente.

---

## 1. Principio base

Si una app cliente no quiere construir su propio login, debe delegar autenticación a Keygo mediante hosted login.

Esto implica que:

- la app cliente inicia un flujo hacia Keygo,
- Keygo autentica al usuario,
- Keygo reutiliza una sesión global si ya existe,
- y luego emite el resultado del flujo para la app correspondiente.

---

## 2. Aclaración conceptual clave

El usuario no “entra a la app porque está logueado en `keygo-ui`”.  
La app cliente **delegó su autenticación** a Keygo, y Keygo reutiliza la sesión global del usuario para completar el acceso.

Esta distinción es importante porque evita mezclar:

- portal del producto,
- experiencia de SSO,
- y sesión de la app cliente.

---

## 3. Flujo recomendado

## 3.1 Inicio del flujo
El usuario entra a una app cliente.

La app detecta que no tiene autenticación local válida y redirige a Keygo, típicamente usando:

- `client_id`
- `redirect_uri`
- `scope`
- `state`
- `code_challenge`
- `code_challenge_method`
- y el contexto del tenant/app cuando corresponda

## 3.2 Validación en Keygo
Keygo revisa si el usuario ya tiene sesión global activa.

### Si no existe sesión global
Muestra el hosted login.

### Si existe sesión global
Reutiliza la sesión y continúa el flujo sin pedir credenciales nuevamente, salvo que la política exija reautenticación.

## 3.3 Verificación de acceso
Antes de emitir el resultado, Keygo debe validar:

- que la app existe,
- que el tenant corresponde,
- que el usuario tiene derecho a acceder,
- y que la política de acceso de la app lo permite.

## 3.4 Entrega del resultado
Keygo devuelve el `authorization code` a la app cliente, y luego la app lo intercambia por tokens.

---

## 4. Separación de sesiones

Esta es una decisión crítica.

## 4.1 Sesión global de Keygo

Representa que el usuario ya está autenticado ante Keygo como plataforma.  
Sirve para:

- hosted login,
- portal de cuenta,
- console,
- y SSO entre apps integradas.

### Características
- suele vivir en cookie o sesión controlada por el dominio de Keygo,
- puede estar asociada a un `sid`,
- puede mantenerse aun cuando una app cliente haya cerrado su propia sesión.

---

## 4.2 Sesión de la app cliente

Representa que el usuario está autenticado para una aplicación concreta.

### Características
- depende del `client_id` o audiencia correspondiente,
- usa sus propios tokens o mecanismos locales de sesión,
- tiene vida propia respecto de la sesión global de Keygo.

---

## 5. Qué no debe ocurrir

No se recomienda tratar ambas sesiones como si fueran la misma cosa.

Si se mezclan, aparecen problemas en:

- logout,
- manejo de múltiples apps abiertas,
- expiración de contexto,
- trazabilidad,
- y revocación.

---

## 6. Logout: criterio recomendado

## 6.1 Logout de app
Cierra la sesión de esa app, pero no necesariamente la sesión global de Keygo.

## 6.2 Logout global
Cierra la sesión global y, según estrategia futura, puede iniciar cierre coordinado de sesiones derivadas o simplemente invalidar la reutilización SSO.

## 6.3 Recomendación para MVP
Partir con una diferenciación clara entre:

- “salir de esta app”
- y “cerrar sesión global”

---

## 7. Políticas de acceso por app

Antes de autorizar acceso, Keygo debe evaluar la política configurada para la app.

Las políticas sugeridas son:

### A. Closed app
Solo entra quien ya tenga membership activa.

### B. Open join
Si la persona ya pertenece al tenant, puede crearse la membership en el primer acceso.

### C. Self-signup
La app permite registro y asociación más abiertos.

## Recomendación de proyecto
Tomar **Closed app** como comportamiento por defecto en el MVP.

---

## 8. Ventajas del hosted login

El hosted login aporta una base sólida para:

- consistencia de UX,
- centralización de seguridad,
- reducción de duplicación en apps cliente,
- MFA futura,
- recuperación de cuenta unificada,
- y SSO real entre aplicaciones.

---

## 9. Consideraciones técnicas recomendadas

Aunque este documento no detalla implementación completa, se recomienda que el diseño se apoye en:

- Authorization Code Flow + PKCE para clientes interactivos,
- manejo explícito de `state` y `nonce`,
- rotación de refresh tokens,
- invalidación de sesiones comprometidas,
- JWKS y firma de tokens controlada,
- y trazabilidad por sesión y app.

---

## 10. Conclusión

El hosted login no debe verse como una simple pantalla compartida, sino como un patrón de autenticación central del producto.

La postura recomendada es:

- sesión global para Keygo,
- sesión propia para cada app cliente,
- y reutilización de la sesión global solo como mecanismo de SSO, no como reemplazo de la sesión de la app.
