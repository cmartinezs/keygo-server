# CLAUDE.md — Reglas para agentes

> Este archivo es para agentes que soportan reglas a nivel de repo (Claude, Copilot agent mode, etc.).
> Si estás usando **GitHub Copilot Chat**, la fuente principal de instrucciones es `.github/copilot-instructions.md`.

## Identidad del proyecto

- Repo: **KeyGo Server** — servicio de identidad/accesos (IAM) open source.
- Stack: Java 21 + Spring Boot, monorepo Maven multi-módulo.
- Arquitectura: Hexagonal / Ports & Adapters.
- Módulo ejecutable: `keygo-run`.

## Reglas de oro

1. **No inventes** estructura del repo: apóyate en los módulos existentes (`keygo-api`, `keygo-app`, etc.).
2. Mantén `keygo-domain` **libre de dependencias Spring** y de otros módulos del proyecto.
3. Cualquier endpoint REST debe:
   - Estar en `keygo-api`.
   - Usar `BaseResponse<T>` como envelope.
   - Emitir `ResponseCode` apropiado.
4. No asumas paths sin `/keygo-server` — hay `context-path` activo.
5. **Nunca** agregues secretos, tokens ni `.env` a Git.
6. Antes de dar por finalizado un cambio, sugiere siempre:
   ```bash
   ./mvnw test
   ./mvnw clean package
   ```

## Cómo trabajar al implementar una feature

1. **Diseño mínimo:** describe clases, módulos afectados y flujo antes de generar código.
2. **Cambios pequeños:** genera un commit lógico por vez.
3. **Tests:** agrega tests unitarios (JUnit 5 + Mockito/AssertJ).
4. **Docs:** actualiza `README.md` o `ARCHITECTURE.md` si cambian APIs o configuración.

## Módulos y sus roles

| Módulo | Rol |
|---|---|
| `keygo-domain` | Dominio puro. Sin Spring. |
| `keygo-app` | Usecases + puertos (interfaces OUT). |
| `keygo-infra` | Implementaciones de puertos. |
| `keygo-api` | REST controllers + DTOs + error handlers. |
| `keygo-supabase` | JPA/Flyway + entidades + repos de Supabase. |
| `keygo-run` | Main + wiring + `application.yml`. |
| `keygo-bom` | Gestión de versiones de dependencias. |

## Conocimiento específico útil

- Supabase/DB se habilita con perfil `supabase` en `SPRING_PROFILES_ACTIVE`.
- Scripts de DB local en `keygo-supabase/scripts/`.
- `KEYGO_ADMIN_KEY` protege `/api/**` vía header `X-KEYGO-ADMIN` — default `changeMe` solo para dev.
- El filtro `BootstrapAdminKeyFilter` puede tener problemas de matching con `context-path`: siempre validar.

## Ejemplo de prompt interno recomendado

```
Implementa la feature X siguiendo la arquitectura hexagonal del repo.
Asegúrate de que compile y tenga tests unitarios.
Si tocas endpoints, documenta considerando context-path=/keygo-server.
No introduzcas secretos ni dependencias innecesarias.
Al finalizar, indica los comandos exactos para verificar (build + tests).
```

