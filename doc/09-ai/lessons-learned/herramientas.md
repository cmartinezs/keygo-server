# Lecciones — Herramientas y Proceso

Editor de código, Maven, Mermaid, Lombok y proceso de trabajo.

---

### [2026-04-04] `replace_string_in_file` deja código duplicado al reemplazar solo el inicio de una clase

**Problema:** Al usar `replace_string_in_file` para reemplazar solo las líneas de imports (inicio de la clase), el resto del cuerpo viejo quedó concatenado después del nuevo cuerpo, resultando en dos definiciones de clase en el mismo archivo (error de compilación "Duplicate class").

**Solución / Buena práctica:** Cuando se reescribe completamente una clase, usar el Write tool para sobrescribir el archivo completo. Alternativamente, incluir suficiente contexto en el `old_string` para abarcar todo el contenido que debe eliminarse.

---

### [2026-03-31] `replace_string_in_file`: verificar que no queden restos del archivo anterior

**Síntoma:** Test no compila: "Duplicate class".

**Causa:** `replace_string_in_file` reemplazó solo el inicio del archivo; el contenido original quedó al final.

**Solución:** El `old_string` debe incluir el bloque completo de apertura de clase + todo el contenido anterior. Para reescrituras totales, usar el Write tool directamente.

---

### [2026-03-29] `replace_string_in_file` elimina import al usarlo como `old_string`

**Síntoma:** Test deja de compilar: import eliminado.

**Causa:** La herramienta reemplaza el `old_string`; usar un import existente como `old_string` lo elimina.

**Solución:** Incluir TODOS los imports (existentes + nuevos) en `new_string`. Verificar con `./mvnw -pl <módulo> compile`.

---

### [2026-03-29] Imports duplicados al añadir anotaciones OpenAPI

**Síntoma:** Nuevos imports insertados antes de los existentes, duplicándolos; warnings de compilación.

**Causa:** No se verificó el bloque de imports antes de agregar.

**Solución:** Revisar bloque completo de imports antes de agregar. Orden: `api.*` → `app.*` → `domain.*` → librerías externas → `java.*`. Compilar con `-pl <módulo> compile` después.

---

### [2026-03-29] Scripts de DB usaban `mvn` sin `-pl keygo-supabase`

**Síntoma:** Scripts fallaban con "command not found" o ejecutaban Flyway en todos los módulos.

**Solución:** Usar `"$PROJECT_ROOT/mvnw" -pl keygo-supabase --no-transfer-progress`. Si se necesita `flyway:clean`, agregar `<cleanDisabled>false</cleanDisabled>` en pom.

**Archivos clave:** `migrate.sh`, `keygo-supabase/pom.xml`

---

### [2026-03-28] Lombok debe declararse explícitamente en cada módulo

**Síntoma:** IntelliJ reporta "symbol not found" para `@Getter`/`@Builder` en `keygo-app`.

**Causa:** Maven compila porque Lombok transita desde `keygo-domain`; IntelliJ no usa transitividad.

**Solución:** Declarar `lombok` scope `provided` en CADA módulo que use anotaciones Lombok. Configurar `annotationProcessorPaths` en `maven-compiler-plugin`.

**Archivos clave:** `keygo-app/pom.xml`

---

### [2026-03-28] `List.of()` sin tipo genérico explícito falla en Java 21

**Síntoma:** Error de compilación en expresión ternaria con `List.of()` en rama vacía.

**Causa:** Java infiere `List<Object>` cuando la rama alternativa tiene tipo diferente.

**Solución:** Usar `List.<TipoEsperado>of()` con tipo explícito.

---

### [2026-03-27] `replace_string_in_file` con `...existing code...` elimina sección entera

**Síntoma:** Bloque completo eliminado del FRONTEND_DEVELOPER_GUIDE.

**Causa:** La herramienta interpretó `...existing code...` como contenido real a reemplazar.

**Solución:** Nunca usar `...existing code...` como marcador. Actualizar secciones por separado o incluir el bloque completo en `old_string`.

---

### [2026-03-27] `replace_string_in_file` con solo imports como `old_string` duplica la clase

**Síntoma:** Dos declaraciones `public class` en el mismo archivo.

**Causa:** Reemplazo parcial concatenó nuevo contenido al principio dejando el original al final.

**Solución:** Para reescrituras totales usar Write tool. O incluir `package + imports + body` completo en `old_string`.

---

### [2026-03-25] Mermaid: signos de interrogación invertidos rompen el parser

**Síntoma:** Validador reporta error en bloque Mermaid con `¿Corregida?` en nodo.

**Causa:** El parser Mermaid no soporta caracteres especiales en labels de nodos.

**Solución:** Usar labels ASCII-safe: `Corregida?` sin caracteres invertidos.

---

### [2026-03-23] Variables de entorno en `application.yml` no se propagan a `.env` automáticamente

**Síntoma:** `SMTP_HOST`, `SMTP_PORT`, etc. configuradas en `application.yml` pero sin documentar en `.env*`.

**Causa:** Sin mecanismo automático de sincronización.

**Solución:** Regla: si aparece en `application.yml` como `${VAR:default}`, debe estar en `.env.example`, `.env-local`, `.env-desa`, `.env-prod`, `ENVIRONMENT_SETUP.md` y `quick-start.sh`.
