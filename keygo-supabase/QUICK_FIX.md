# KeyGo Supabase - Guía Rápida de Solución IntelliJ / Quick IntelliJ Fix Guide

## 🔧 Solución Aplicada / Applied Fix

Se corrigió el problema de compilación en IntelliJ agregando la configuración del procesador de anotaciones de Lombok al `pom.xml` del módulo `keygo-supabase`.

Fixed the compilation issue in IntelliJ by adding the Lombok annotation processor configuration to the `keygo-supabase` module's `pom.xml`.

## ✅ Pasos Rápidos / Quick Steps

### 1. Instalar Plugin Lombok en IntelliJ
**File > Settings > Plugins** → Buscar "Lombok" → Instalar

### 2. Habilitar Procesamiento de Anotaciones
**File > Settings > Build, Execution, Deployment > Compiler > Annotation Processors**
- ✅ Enable annotation processing
- ✅ Obtain processors from project classpath

### 3. Recargar Maven
**View > Tool Windows > Maven** → Click en "Reload All Maven Projects" 🔄

### 4. Rebuild Project
**Build > Rebuild Project**

### 5. (Opcional) Invalidar Cachés si persiste el problema
**File > Invalidate Caches...** → Invalidate and Restart

## 🎯 Cambios en el Código / Code Changes

### `keygo-supabase/pom.xml`

Se agregó la configuración del `maven-compiler-plugin`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.13.0</version>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
            <path>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-configuration-processor</artifactId>
                <version>${spring-boot.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

## 🚀 Verificación / Verification

### Terminal
```bash
cd keygo-supabase
../mvnw clean compile
```
✅ BUILD SUCCESS

### IntelliJ
1. Click derecho en `keygo-supabase`
2. **Maven > Lifecycle > package**
3. ✅ Debe compilar sin errores

## 📚 Más Información / More Information

Ver `INTELLIJ_FIX.md` para la guía completa con más detalles y troubleshooting.

See `INTELLIJ_FIX.md` for the complete guide with more details and troubleshooting.

