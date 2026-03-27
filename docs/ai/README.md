# Documentación AI — KeyGo Server

> Categoría de documentos de **base de conocimiento para agentes AI** (Copilot, Claude, etc.).
>
> Los archivos de esta carpeta son mantenidos por el agente de forma continua — **no requieren
> orden explícita del usuario** para actualizarse.

---

## Documentos

| Documento | Descripción | Actualización |
|---|---|---|
| [lecciones.md](lecciones.md) | Errores resueltos, buenas prácticas y convenciones adoptadas durante el trabajo del agente | Continua — al concluir tarea con error/patrón nuevo |
| [propuestas.md](propuestas.md) | Propuestas técnicas y funcionales organizadas por horizonte temporal | Continua — al detectar propuesta nueva o completarla |
| [inconsistencias.md](inconsistencias.md) | Centralizador de inconsistencias detectadas entre docs y código/DB | Continua — al detectar inconsistencia |
| [inconsistencias-datos.md](inconsistencias-datos.md) | Detalle de inconsistencias en el modelo de datos / schema DB | Continua — al auditar migraciones |
| [agents-registro.md](agents-registro.md) | Historial detallado de cambios en módulos, comandos, patrones y URLs del quick-start | Continua — al cambiar estructura del repo |

---

## Relación con documentos raíz

Los documentos AI de la raíz del repositorio actúan como **resúmenes de referencia rápida** con
enlaces a los detalles en esta carpeta:

| Documento raíz | Detalle en docs/ai/ |
|---|---|
| [`AI_CONTEXT.md`](../../AI_CONTEXT.md) → sub-doc lecciones | [`lecciones.md`](lecciones.md) |
| [`AI_CONTEXT.md`](../../AI_CONTEXT.md) → sub-doc propuestas | [`propuestas.md`](propuestas.md) |
| [`AGENTS.md`](../../AGENTS.md) → registro de cambios | [`agents-registro.md`](agents-registro.md) |
| [`INCONSISTENCIAS.md`](../../INCONSISTENCIAS.md) → detalle datos | [`inconsistencias-datos.md`](inconsistencias-datos.md) |

---

## Reglas de actualización del agente

1. **Al concluir cualquier tarea**: evaluar si ocurrió un error, mejor patrón, propuesta o inconsistencia.
2. **Si ocurrió**: actualizar el documento correspondiente en esta carpeta **antes de cerrar la tarea**.
3. **Formato de entradas**: `### [YYYY-MM-DD] Título descriptivo` seguido de campos estándar.
4. **No requiere orden explícita**: estos documentos son parte del ciclo de trabajo del agente.

---

## Verificación de actividad

```bash
# Verifica que los documentos AI tengan actividad reciente (umbral: 30 días)
./docs/scripts/check-ai-docs.sh

# Cambiar umbral
./docs/scripts/check-ai-docs.sh --days 60

# Solo exit code (útil en CI)
./docs/scripts/check-ai-docs.sh --quiet
```

---

**Responsable:** AI Agent | **Mantenimiento:** Continuo

