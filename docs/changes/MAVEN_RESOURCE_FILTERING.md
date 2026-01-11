# Maven Resource Filtering Configuration / Configuración de Filtrado de Recursos Maven

[English](#english) | [Español](#español)

---

## English

### What is it?

Maven resource filtering allows you to replace placeholders in your resource files (like `application.yml`) with values from your `pom.xml` during the build process.

### Configuration Applied

**File:** `keygo-run/pom.xml`

Added resource filtering:
```xml
<build>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
        </resource>
    </resources>
    <!-- ...plugins... -->
</build>
```

**File:** `keygo-run/src/main/resources/application.yml`

Using Maven properties:
```yaml
key-go-server:
  title: "@project.parent.name@"
  version: "@project.parent.version@"
```

### How it Works

1. **Build time:** Maven reads the parent POM values:
   - `@project.parent.name@` → "KeyGo Server"
   - `@project.parent.version@` → "1.0-SNAPSHOT"

2. **Processing:** Maven replaces placeholders in `application.yml`

3. **Runtime:** Spring Boot reads the processed file from `target/classes/`:
   ```yaml
   key-go-server:
     title: "KeyGo Server"
     version: "1.0-SNAPSHOT"
   ```

### Benefits

✅ Single source of truth (POM file)
✅ No need to update version in multiple places
✅ Automatic synchronization between Maven and application
✅ Less maintenance and fewer errors

### Available Variables

- `@project.parent.name@` - Parent project name
- `@project.parent.version@` - Parent project version
- `@project.name@` - Current module name
- `@project.version@` - Current module version
- `@project.description@` - Project description
- Any custom property from POM

---

## Español

### ¿Qué es?

El filtrado de recursos de Maven permite reemplazar marcadores de posición en tus archivos de recursos (como `application.yml`) con valores de tu `pom.xml` durante el proceso de compilación.

### Configuración Aplicada

**Archivo:** `keygo-run/pom.xml`

Se agregó filtrado de recursos:
```xml
<build>
    <resources>
        <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
        </resource>
    </resources>
    <!-- ...plugins... -->
</build>
```

**Archivo:** `keygo-run/src/main/resources/application.yml`

Usando propiedades de Maven:
```yaml
key-go-server:
  title: "@project.parent.name@"
  version: "@project.parent.version@"
```

### Cómo Funciona

1. **Tiempo de compilación:** Maven lee los valores del POM padre:
   - `@project.parent.name@` → "KeyGo Server"
   - `@project.parent.version@` → "1.0-SNAPSHOT"

2. **Procesamiento:** Maven reemplaza los marcadores en `application.yml`

3. **Tiempo de ejecución:** Spring Boot lee el archivo procesado desde `target/classes/`:
   ```yaml
   key-go-server:
     title: "KeyGo Server"
     version: "1.0-SNAPSHOT"
   ```

### Beneficios

✅ Única fuente de verdad (archivo POM)
✅ No necesitas actualizar la versión en múltiples lugares
✅ Sincronización automática entre Maven y la aplicación
✅ Menos mantenimiento y menos errores

### Variables Disponibles

- `@project.parent.name@` - Nombre del proyecto padre
- `@project.parent.version@` - Versión del proyecto padre
- `@project.name@` - Nombre del módulo actual
- `@project.version@` - Versión del módulo actual
- `@project.description@` - Descripción del proyecto
- Cualquier propiedad personalizada del POM

---

**Note / Nota:** The `@` delimiters are used instead of `${}` to avoid conflicts with Spring property placeholders / Los delimitadores `@` se usan en lugar de `${}` para evitar conflictos con los marcadores de propiedades de Spring.

