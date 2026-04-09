# Desarrollo — Índice

**Propósito:** Guía práctica para setup local, testing, debugging, IDE y troubleshooting.

Todo lo que necesitas para desarrollar, testear y debuggear localmente.

---

## 🚀 Quick-Start (Primero)

| Tarea | Documento | Tiempo |
|---|---|---|
| Configurar ambiente local | `ENVIRONMENT_SETUP.md` | 30 min |
| Clonar, compilar, ejecutar | `ENVIRONMENT_SETUP.md` (scripts) | 10 min |
| Generar código IDE | `ide/INTELLIJ.md` o `ide/VSCODE.md` | 5 min |

---

## 🎯 Búsqueda Rápida

| Pregunta | Documento |
|---|---|
| ¿Cómo instalo el ambiente? | `ENVIRONMENT_SETUP.md` |
| ¿Por qué mi código no compila? | `troubleshooting/common-issues.md` |
| ¿Cómo debuggeo un error? | `DEBUG_GUIDE.md` |
| ¿Cómo escribo tests unitarios? | `testing/UNIT_TESTING.md` |
| ¿Cómo escribo integration tests? | `testing/INTEGRATION_TESTING.md` |
| ¿Cómo configuro mi IDE? | `ide/INTELLIJ.md` o `ide/VSCODE.md` |

---

## 📁 Estructura

### ⚙️ Setup y Entorno

| Documento | Descripción |
|---|---|
| `ENVIRONMENT_SETUP.md` | Variables, Docker, migraciones, scripts |
| `CODE_STYLE.md` | Convenciones: naming, formatting, imports |

### 🧪 Testing

**Índice:** `testing/README.md`

| Documento | Descripción |
|---|---|
| `TEST_STRATEGY.md` | Estrategia general: unitarios, integración, E2E |
| `UNIT_TESTING.md` (nuevo Sprint 3) | Mocking, fixtures, patrones |
| `INTEGRATION_TESTING.md` (nuevo Sprint 3) | Testcontainers, H2, @SpringBootTest |
| `TESTCONTAINERS_GUIDE.md` (nuevo Sprint 3) | PostgreSQL en container, setup |

### 🐛 Debugging

| Documento | Descripción |
|---|---|
| `DEBUG_GUIDE.md` (nuevo Sprint 1) | Logs, DEBUG mode, inspeccionar BD, JWT |
| `troubleshooting/README.md` | Matriz síntoma → solución |
| `troubleshooting/common-issues.md` | Errores frecuentes + soluciones |

### 🖥️ IDE y Herramientas

**Índice:** `ide/README.md`

| Documento | Descripción |
|---|---|
| `INTELLIJ.md` | Setup, plugins, keybindings, debug |
| `VSCODE.md` | Setup, extensions, debug |
| `KEYBINDINGS.md` (nuevo) | Atajos personalizados |

---

## Navegación Jerárquica

```
development/
├── README.md (este índice)
├── ENVIRONMENT_SETUP.md
├── CODE_STYLE.md
├── DEBUG_GUIDE.md (nuevo)
│
├── testing/
│   ├── README.md (índice)
│   ├── TEST_STRATEGY.md
│   ├── UNIT_TESTING.md (nuevo)
│   ├── INTEGRATION_TESTING.md (nuevo)
│   └── TESTCONTAINERS_GUIDE.md (nuevo)
│
├── ide/
│   ├── README.md (índice)
│   ├── INTELLIJ.md
│   ├── VSCODE.md
│   └── KEYBINDINGS.md (nuevo)
│
└── troubleshooting/
    ├── README.md (matriz síntomas)
    └── common-issues.md
```

---

## 📊 Estado de Documentos

### ✅ Existentes
- `ENVIRONMENT_SETUP.md` — Setup local, variables, Docker
- `CODE_STYLE.md` — Convenciones de código
- `TEST_STRATEGY.md` — Estrategia de testing

### 🔲 Nuevos (Sprint 1-3)
- `DEBUG_GUIDE.md` (Sprint 1) — Debugging hands-on
- `testing/UNIT_TESTING.md` (Sprint 3) — Patrones unitarios
- `testing/INTEGRATION_TESTING.md` (Sprint 3) — Testcontainers
- `testing/TESTCONTAINERS_GUIDE.md` (Sprint 3) — Setup PostgreSQL
- `ide/KEYBINDINGS.md` (Sprint 2) — Atajos personalizados

---

## 🔄 Workflows Típicos

### Nuevo dev en el proyecto

```
1. Leer: ENVIRONMENT_SETUP.md (30 min)
2. Ejecutar: ./docs/scripts/quick-start.sh (10 min)
3. Configurar: IDE/INTELLIJ.md o IDE/VSCODE.md (5 min)
4. Escribir: Primera tarea
5. Consultar: troubleshooting/common-issues.md si hay error
```

### Implementar feature

```
1. Leer: docs/design/patterns/README.md (patrones)
2. Código: Seguir patrón
3. Test: testing/UNIT_TESTING.md + testing/INTEGRATION_TESTING.md
4. Debug: Si falla, DEBUG_GUIDE.md
5. Terminar: Agregar lección aprendida a docs/ai/lecciones/
```

### Debuggear error

```
1. Error → troubleshooting/common-issues.md (¿está documentado?)
2. Si no → DEBUG_GUIDE.md (cómo investigar)
3. Leer logs, activar DEBUG, inspeccionar BD
4. Solucionar
5. Documentar lección
```

---

## 🧰 Scripts Útiles

Desde `docs/scripts/`:

```bash
./quick-start.sh             # Setup completo (primera vez)
./keygo.sh                   # Ejecutar aplicación
./db/start.sh               # Iniciar PostgreSQL
./db/migrate.sh             # Ejecutar migraciones Flyway
./db/psql.sh                # Conectar a BD
./test-service-info.sh      # Probar /service-info
./test-response-codes.sh    # Probar ResponseCode
```

---

## 📚 Referencias Cruzadas

- **Patrones:** [`../design/patterns/README.md`](../design/patterns/README.md)
- **Lecciones:** [`../ai/lecciones/README.md`](../ai/lecciones/README.md)
- **Planes:** [`../plans/README.md`](../plans/README.md)
- **Arquitectura:** [`../design/core/ARCHITECTURE.md`](../design/core/ARCHITECTURE.md)
- **Inconsistencias:** [`../ai/inconsistencies/README.md`](../ai/inconsistencies/README.md)

---

**Última actualización:** 2026-04-09  
**Estado:** Reorganización en curso + nuevos documentos (Sprint 1-3)  
**Responsable:** AI Agent
