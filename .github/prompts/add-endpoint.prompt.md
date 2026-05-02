---
mode: agent
---

# Agregar endpoint REST (Hexagonal)

Objetivo: crear un endpoint REST nuevo siguiendo la arquitectura hexagonal del repo.

## Instrucciones

1. Crea el controller en `keygo-api` con rutas bajo `/api/v1/...`.
2. El controller debe devolver `BaseResponse<T>` y emitir el `ResponseCode` apropiado.
3. Crea o reutiliza un usecase en `keygo-app`.
4. Define un puerto OUT (interface) si el usecase necesita IO (DB, API externa, etc.).
5. Las implementaciones concretas van en `keygo-infra` o `keygo-supabase` según corresponda.
6. Mantén `keygo-domain` libre de dependencias Spring y de otros módulos del proyecto.
7. Agrega tests unitarios (JUnit 5 + Mockito/AssertJ).
8. Recuerda: el path real incluye `context-path=/keygo-server` — documenta y testea endpoints con el path completo.

## Entrega esperada

- Lista de clases nuevas/modificadas (con módulo).
- Código completo de cada clase.
- Tests unitarios.
- Comandos de verificación:

```bash
./mvnw test
./mvnw clean package
```

