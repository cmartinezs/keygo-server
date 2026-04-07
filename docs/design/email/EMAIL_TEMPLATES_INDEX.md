# Email Templates en KeyGo — Documentación Centralizada

> **Índice de documentos:** Guías, patrones, best practices, y referencias para implementar emails con Thymeleaf.
>
> **Actualizado:** 2026-04-03  
> **Estado:** Completo (4 documentos + este índice)

---

## 📖 Documentos disponibles

### 1. **Quick Start (⚡ Recomendado para empezar)**

**Archivo:** `docs/design/EMAIL_TEMPLATES_QUICKSTART.md`

- **Tiempo:** 30 minutos
- **Contenido:** 5 pasos concretos para implementar
- **Nivel:** Principiante → Intermedio
- **Incluye:**
  - Agregar dependencia Thymeleaf
  - Crear config `ThymeleafTemplateConfig`
  - Crear primer template HTML
  - Inyectar en servicio
  - Configurar SMTP (local, staging, prod)
  - Test rápido de verificación
- **Cuándo usarlo:** Cuando necesitas implementar YA

---

### 2. **Guía Completa (📚 Referencia técnica)**

**Archivo:** `docs/design/EMAIL_TEMPLATES_THYMELEAF.md`

- **Tiempo:** Lectura profunda (1-2 horas)
- **Contenido:** Arquitectura, análisis, implementación paso a paso
- **Nivel:** Intermedio → Senior
- **Incluye:**
  - Análisis del patrón usado en proyecto anterior
  - Fortalezas y limitaciones
  - Consideraciones Spring Boot 4.x + Jackson 3
  - Arquitectura hexagonal completa
  - Estructura de directorios
  - 8 pasos de implementación con código completo
  - Ejemplos prácticos (casos reales)
  - Best practices industria 2024-2026
  - Testing (3 niveles: unit, integration, E2E)
  - Migración desde HTML inline
  - Checklist de implementación
  - Referencias y roadmap futuro
- **Cuándo usarlo:** Entender en profundidad, diseñar solución, mentoring

---

### 3. **Análisis Comparativo (🔍 Decisiones arquitectónicas)**

**Archivo:** `docs/design/EMAIL_PATTERNS_ANALYSIS.md`

- **Tiempo:** 45 minutos
- **Contenido:** Patrones, ventajas/desventajas, best practices industry
- **Nivel:** Senior / Architects
- **Incluye:**
  - Comparativa 4 patrones (HTML inline, Thymeleaf, Freemarker, Custom DSL)
  - Best practices industria 2024-2026:
    - Arquitectura de email service
    - Testing strategy (3 niveles)
    - Performance y caché
    - Seguridad (XSS, injection)
    - i18n (internacionalización)
    - Resiliencia (retry + circuit breaker)
    - Observabilidad (logging + metrics)
  - Roadmap implementación KeyGo (6 fases)
  - Análisis empresa industria (Spotify, Netflix, Stripe, Auth0, Okta)
  - Plan de migración de proyecto existente
- **Cuándo usarlo:** Tomar decisiones arquitectónicas, presentar a team leads

---

### 4. **Referencia Visual (🎯 Quick lookup)**

**Archivo:** `docs/design/EMAIL_TEMPLATES_VISUAL.md`

- **Tiempo:** 10-15 minutos
- **Contenido:** Diagramas, checklists, flujos
- **Nivel:** Todos
- **Incluye:**
  - Diagrama de arquitectura (visión alto nivel)
  - Estructura de ficheros
  - Checklist implementación (4 fases)
  - Flujo de uso típico
  - Testing por nivel (con snippets)
  - Referencias cruzadas
  - Cómo agregar nuevo tipo de email
  - Configuración SMTP por ambiente
  - Roadmap futuro
  - Troubleshooting
- **Cuándo usarlo:** Búsqueda rápida, while coding, debugging

---

## 🎯 Cómo usarlos

### Escenario 1: "Necesito implementar emails ahora"

1. Leer: `EMAIL_TEMPLATES_QUICKSTART.md` (30 min)
2. Implementar: Seguir 5 pasos paso a paso
3. Referencia: Tener abierto `EMAIL_TEMPLATES_VISUAL.md` para troubleshooting

---

### Escenario 2: "Necesito entender la arquitectura"

1. Leer: `EMAIL_TEMPLATES_THYMELEAF.md` § "Arquitectura del patrón Thymeleaf" (20 min)
2. Leer: `EMAIL_PATTERNS_ANALYSIS.md` § "Best Practices Industria" (30 min)
3. Diagrama: `EMAIL_TEMPLATES_VISUAL.md` § "Arquitectura (Visión de Alto Nivel)"

---

### Escenario 3: "Necesito decidir si usar Thymeleaf"

1. Leer: `EMAIL_PATTERNS_ANALYSIS.md` § "Comparación de patrones" (20 min)
2. Leer: `EMAIL_PATTERNS_ANALYSIS.md` § "¿Quién usa Thymeleaf?" (5 min)
3. Conclusión: `EMAIL_PATTERNS_ANALYSIS.md` § "Para KeyGo Server"

---

### Escenario 4: "Estoy debugueando un problema"

1. Ir a: `EMAIL_TEMPLATES_VISUAL.md` § "Troubleshooting"
2. Si no resuelve, leer: `EMAIL_TEMPLATES_THYMELEAF.md` § "Testing"

---

### Escenario 5: "Necesito agregar nuevo tipo de email"

1. Recordar: `EMAIL_TEMPLATES_VISUAL.md` § "Extensión: Agregar nuevo tipo"
2. Referencia: `EMAIL_TEMPLATES_THYMELEAF.md` § "Ejemplos prácticos"
3. Testing: `EMAIL_TEMPLATES_VISUAL.md` § "Testing por nivel"

---

## 📊 Matriz de contenido

| Documento | Quickstart | Arquitectura | Security | Testing | Ejemplos | Best Practices | Roadmap |
|---|---|---|---|---|---|---|---|
| **QUICKSTART** | ✅ Completo | ⚠️ Mínimo | - | - | ✅ Sí | - | - |
| **THYMELEAF** | - | ✅ Completo | ✅ Sí | ✅ Completo | ✅ Detallado | ✅ Sí | ✅ Sí |
| **ANALYSIS** | - | ✅ Comparativa | ✅ Sí | ✅ Comparado | ✅ Benchmark | ✅ Industria | ✅ Completo |
| **VISUAL** | - | ✅ Diagrama | - | ✅ Snippets | ✅ Checklist | - | ✅ Básico |

---

## 🚀 Guía de lectura recomendada

### Para **nuevos developers** (onboarding)

```
1. EMAIL_TEMPLATES_QUICKSTART.md                (30 min)
2. EMAIL_TEMPLATES_VISUAL.md                    (15 min)
3. EMAIL_TEMPLATES_THYMELEAF.md (opcional)      (1-2 h)
```

### Para **tech leads / architects**

```
1. EMAIL_TEMPLATES_THYMELEAF.md § Arquitectura        (20 min)
2. EMAIL_PATTERNS_ANALYSIS.md                         (45 min)
3. EMAIL_TEMPLATES_VISUAL.md § Roadmap                (10 min)
```

### Para **code reviewers**

```
1. EMAIL_TEMPLATES_THYMELEAF.md § Checklist de reglas críticas
2. EMAIL_TEMPLATES_VISUAL.md § Testing por nivel
3. EMAIL_PATTERNS_ANALYSIS.md § Security
```

### Para **future maintenance**

```
1. Guardar esta página (INDEX) como bookmark
2. EMAIL_TEMPLATES_VISUAL.md para búsqueda rápida
3. EMAIL_TEMPLATES_THYMELEAF.md para contexto profundo
```

---

## 🔗 Integración con proyecto

### Referenciado en:

- ✅ `AGENTS.md` § "Email notifications — Thymeleaf templates"
- ✅ `docs/design/` (todos en la carpeta design/)

### Próximos updates:

- [ ] Agregar entrada en `docs/ai/lecciones.md` (después de implementación)
- [ ] Registrar propuestas futuras en `docs/ai/propuestas.md` (T-XXX)
- [ ] Actualizar `ROADMAP.md` si se inicia implementación

---

## 📝 Notas técnicas clave

### Jackson 3 (Spring Boot 4.x)

Asegurarse de usar imports correctos:

```java
// ✅ Correcto
import tools.jackson.databind.ObjectMapper;

// ❌ Incorrecto (Spring Boot 3.x — no compila en SB4)
import com.fasterxml.jackson.databind.ObjectMapper;
```

### Thymeleaf + Spring Boot 4.x

No hay cambios de API. Thymeleaf 3.2.x es compatible con Spring Boot 4.x sin cambios.

```java
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;  // ← Spring 6 (SB4)
```

### Dependencies

```xml
<!-- Mínimo necesario en keygo-run/pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>

<!-- Automático vía spring-boot-starter -->
<!-- - jakarta.mail -->
<!-- - spring-boot-starter-mail -->
```

---

## ✍️ Historial de creación

| Fecha | Documento | Autor | Status |
|---|---|---|---|
| 2026-04-03 | EMAIL_TEMPLATES_THYMELEAF.md | AI Agent | ✅ Completo |
| 2026-04-03 | EMAIL_TEMPLATES_QUICKSTART.md | AI Agent | ✅ Completo |
| 2026-04-03 | EMAIL_PATTERNS_ANALYSIS.md | AI Agent | ✅ Completo |
| 2026-04-03 | EMAIL_TEMPLATES_VISUAL.md | AI Agent | ✅ Completo |
| 2026-04-03 | INDEX (este documento) | AI Agent | ✅ Completo |
| 2026-04-03 | AGENTS.md § Email notifications | AI Agent | ✅ Actualizado |

---

## 📞 Contacto / Preguntas

Si tienes dudas sobre:

- **Implementación rápida** → Ver `EMAIL_TEMPLATES_QUICKSTART.md`
- **Arquitectura** → Ver `EMAIL_TEMPLATES_THYMELEAF.md`
- **Decisiones técnicas** → Ver `EMAIL_PATTERNS_ANALYSIS.md`
- **Búsqueda rápida** → Ver `EMAIL_TEMPLATES_VISUAL.md`

O busca en `AGENTS.md` § "Email notifications".

---

**Versión:** 1.0  
**Última actualización:** 2026-04-03  
**Revisado por:** AI Development Team — KeyGo Server


