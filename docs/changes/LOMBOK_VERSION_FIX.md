# Fix Lombok TypeTag Error in IntelliJ / Corrección Error TypeTag de Lombok en IntelliJ

## Fecha / Date
2026-01-12

## Problema / Problem

IntelliJ IDEA mostraba el siguiente error al compilar:
```
java: java.lang.ExceptionInInitializerError
com.sun.tools.javac.code.TypeTag::UNKNOWN
```

IntelliJ IDEA was showing the following error when compiling:
```
java: java.lang.ExceptionInInitializerError
com.sun.tools.javac.code.TypeTag::UNKNOWN
```

- Maven compilaba correctamente / Maven compiled successfully
- Annotation Processing estaba habilitado / Annotation Processing was enabled
- Plugin de Lombok instalado / Lombok plugin installed
- Java 21 con Spring Boot 4.0.1

## Causa Raíz / Root Cause

Conflicto de versiones de Lombok:
- `maven-compiler-plugin` especificaba Lombok **1.18.34** (hardcoded)
- Spring Boot 4.0.1 parent gestiona Lombok **1.18.42**
- IntelliJ detectaba esta inconsistencia causando el error

Lombok version conflict:
- `maven-compiler-plugin` specified Lombok **1.18.34** (hardcoded)
- Spring Boot 4.0.1 parent manages Lombok **1.18.42**
- IntelliJ detected this inconsistency causing the error

## Solución Aplicada / Solution Applied

### 1. Eliminada versión hardcodeada de Lombok / Removed hardcoded Lombok version

**Archivos modificados / Modified files:**
- `keygo-api/pom.xml`
- `keygo-common/pom.xml`
- `keygo-run/pom.xml`

**Cambio / Change:**
```xml
<!-- ANTES / BEFORE -->
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.34</version>  <!-- ❌ Hardcoded version -->
    </path>
</annotationProcessorPaths>

<!-- DESPUÉS / AFTER -->
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <!-- ✅ Version managed by Spring Boot parent (1.18.42) -->
    </path>
</annotationProcessorPaths>
```

### 2. Eliminada gestión redundante en BOM / Removed redundant BOM management

**Archivo modificado / Modified file:**
- `keygo-bom/pom.xml`

Eliminada la sección `<dependencyManagement>` que especificaba Lombok 1.18.34, ya que Spring Boot parent ya gestiona la versión.

Removed the `<dependencyManagement>` section that specified Lombok 1.18.34, as Spring Boot parent already manages the version.

## Pasos para IntelliJ / Steps for IntelliJ

Para que IntelliJ reconozca los cambios:

1. **Recargar proyecto Maven / Reload Maven Project:**
   - Abrir la ventana Maven (View → Tool Windows → Maven)
   - Click en el icono "Reload All Maven Projects" (🔄)
   - O: Click derecho en `pom.xml` → Maven → Reload Project

2. **Invalidar caché (si es necesario) / Invalidate Caches (if needed):**
   - File → Invalidate Caches...
   - Marcar "Clear file system cache and Local History"
   - Click "Invalidate and Restart"

3. **Verificar configuración / Verify configuration:**
   - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Asegurar que "Enable annotation processing" esté marcado
   - El processor path debe apuntar a `lombok-1.18.42.jar`

## Verificación / Verification

```bash
# Verificar versión de Lombok usada
./mvnw dependency:tree | grep lombok

# Debería mostrar / Should show:
# org.projectlombok:lombok:jar:1.18.42:provided

# Compilar proyecto
./mvnw clean compile

# Debería compilar sin errores / Should compile without errors
```

## Beneficios / Benefits

✅ Consistencia de versiones entre Maven e IntelliJ
✅ No más conflictos de annotation processors
✅ Gestión centralizada por Spring Boot
✅ Actualizaciones automáticas de Lombok con Spring Boot
✅ Menor mantenimiento del código

✅ Version consistency between Maven and IntelliJ
✅ No more annotation processor conflicts
✅ Centralized management by Spring Boot
✅ Automatic Lombok updates with Spring Boot
✅ Less code maintenance

## Lecciones Aprendidas / Lessons Learned

1. **No hardcodear versiones ya gestionadas por parent POMs** / Don't hardcode versions already managed by parent POMs
2. **Spring Boot parent gestiona versiones de librerías comunes** / Spring Boot parent manages common library versions
3. **IntelliJ es sensible a inconsistencias de versiones en annotation processors** / IntelliJ is sensitive to version inconsistencies in annotation processors
4. **Siempre recargar Maven después de cambios en POMs** / Always reload Maven after POM changes

## Referencias / References

- [Lombok Compatibility](https://projectlombok.org/setup/overview)
- [Spring Boot Dependency Versions](https://docs.spring.io/spring-boot/docs/current/reference/html/dependency-versions.html)
- [Maven Compiler Plugin - Annotation Processing](https://maven.apache.org/plugins/maven-compiler-plugin/compile-mojo.html#annotationProcessorPaths)

