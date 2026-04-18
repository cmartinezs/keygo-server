---
id: T-161
title: Revisar compatibilidad JaCoCo con Java 21
status: 🔲 PENDING
created: 2026-04-15
---

# T-161 — Revisar compatibilidad JaCoCo con Java 21

## Requerimiento

Durante la ejecución de tests con Surefire aparece el siguiente warning:

```
Caused by: java.lang.IllegalArgumentException: Unsupported class file major version 69
    at org.jacoco.agent.rt.internal_aeaf9ab.asm.ClassReader.<init>(ClassReader.java:200)
```

`major version 69` corresponde a Java 21 (`class file format 65 + 4 = 69`). El agente de JaCoCo
incluido en el proyecto no reconoce bytecode compilado con `--release 21`.

Los tests pasan correctamente y la cobertura se reporta, pero el warning indica que JaCoCo
está instrumentando las clases con una versión de ASM que no soporta el nivel de bytecode
objetivo del proyecto. Esto puede producir coverage incompleto o silenciosamente incorrecto
en versiones futuras.

## Análisis preliminar

- Revisar la versión de `jacoco-maven-plugin` declarada en el `pom.xml` raíz.
- La versión mínima con soporte completo para Java 21 (class file version 65) es **JaCoCo 0.8.11**.
- Verificar si el plugin está fijado a una versión anterior o heredado del parent BOM.

## Pasos propuestos

1. `PENDING` — Identificar versión actual de `jacoco-maven-plugin` en `pom.xml`.
2. `PENDING` — Actualizar a `≥ 0.8.11` si la versión actual es inferior.
3. `PENDING` — Re-ejecutar `./mvnw clean verify` y confirmar que el warning desaparece.
4. `PENDING` — Verificar que los umbrales de cobertura (60% instrucciones / 15% `keygo-supabase`) siguen cumpliéndose.
5. `PENDING` — Actualizar `doc/08-reference/data/migrations.md` si aplica; registrar en `lessons-learned.md`.

## Verificación

- `./mvnw clean verify` sin warnings `Unsupported class file major version`.
- Reporte JaCoCo generado en `target/site/jacoco/` con datos completos.
- Build verde con umbrales de cobertura satisfechos.
