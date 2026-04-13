# Lecciones Aprendidas — KeyGo Server

Errores encontrados, buenas prácticas y convenciones adoptadas durante implementaciones.
Consultar antes de implementar. Agregar entrada al concluir cualquier tarea donde ocurra un error, bug o mejor patrón.

---

## Índice

| Archivo | Categoría | Entradas | Última entrada |
|---|---|---|---|
| [persistencia.md](persistencia.md) | JPA, Flyway, DB schema, queries | 17 | 2026-04-13 |
| [tests.md](tests.md) | Tests unitarios, Mockito, cobertura | 14 | 2026-04-07 |
| [seguridad.md](seguridad.md) | Autenticación, autorización, anti-abuso | 9 | 2026-04-07 |
| [spring.md](spring.md) | Spring Boot 4, AOP, Logback, i18n | 10 | 2026-04-06 |
| [api.md](api.md) | REST, OpenAPI/Swagger, excepciones, email, Postman | 18 | 2026-04-05 |
| [dominio.md](dominio.md) | Dominio, casos de uso, decisiones arquitectónicas | 15 | 2026-04-07 |
| [herramientas.md](herramientas.md) | Editor, Maven, Mermaid, Lombok, proceso | 11 | 2026-04-04 |

---

## Formato de entrada

```markdown
### [YYYY-MM-DD] Título descriptivo

**Contexto / Síntoma:** Qué se vio / qué falló.
**Problema / Causa:** Por qué ocurrió.
**Solución / Buena práctica:** Qué hacer en el futuro.
**Archivos clave:** (opcional)
```

---

## Regla

Al concluir cualquier tarea donde ocurra un error, bug, o se identifique un patrón mejor → agregar entrada al archivo de categoría correspondiente antes de cerrar la conversación.
