# Estrategia de Dependencias de Test / Test Dependencies Strategy

## Pregunta Original

**¿Por qué no poner las dependencias de test en `keygo-common` si todos los módulos dependen de él?**

## 🎯 Respuesta Técnica

### El Problema con Dependencias Transitivas

Las dependencias con **scope `test`** NO se propagan transitivamente en Maven:

```xml
<!-- En keygo-common -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

Si otro módulo depende de `keygo-common`:
```xml
<!-- En keygo-domain -->
<dependency>
    <groupId>io.cmartinezs.keygo</groupId>
    <artifactId>keygo-common</artifactId>
</dependency>
```

❌ **keygo-domain NO heredará JUnit** porque `test` scope no es transitivo.

---

## ✅ Solución Implementada: DependencyManagement en POM Padre

### Estrategia en 2 Niveles

#### 1. **POM Padre** - Gestión Centralizada de Versiones

```xml
<!-- keygo-server/pom.xml -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <scope>test</scope>
        </dependency>
        <!-- ... otros -->
    </dependencies>
</dependencyManagement>
```

**Ventaja:** Las versiones se gestionan en UN solo lugar (heredadas de Spring Boot parent)

#### 2. **Módulos Hijos** - Declaración Sin Versión

```xml
<!-- keygo-app/pom.xml -->
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
        <!-- NO version - se toma del parent -->
    </dependency>
</dependencies>
```

**Ventaja:** Cada módulo declara lo que necesita, pero la versión está centralizada

---

## 📊 Comparación de Estrategias

### Opción 1: Todo en keygo-common ❌
```
keygo-common
  └─ JUnit (test scope)
  └─ AssertJ (test scope)

keygo-domain
  └─ depends on keygo-common
  └─ ❌ NO tiene JUnit (test no es transitivo)
```

**Problema:** No funciona porque `test` scope no es transitivo

---

### Opción 2: Duplicar en cada módulo (mi primera versión) ⚠️
```
keygo-app/pom.xml:
  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
  </dependency>

keygo-api/pom.xml:
  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
  </dependency>

... (repetido en todos)
```

**Problema:** Duplicación, pero funcionaba

**Ventaja:** Explícito, cada módulo declara sus dependencias

---

### Opción 3: DependencyManagement en padre ✅ (MEJOR)
```
keygo-server/pom.xml (padre):
  <dependencyManagement>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencyManagement>

keygo-app/pom.xml:
  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
    <!-- versión gestionada por padre -->
  </dependency>
```

**Ventaja:** 
- ✅ Versiones centralizadas en el padre
- ✅ Cada módulo declara lo que necesita
- ✅ No hay duplicación de versiones
- ✅ Fácil actualización (solo en un lugar)

---

## 🎓 Lección Aprendida

### Maven Dependency Scopes

| Scope | Compilación | Runtime | Test | Transitivo |
|-------|-------------|---------|------|------------|
| `compile` | ✅ | ✅ | ✅ | ✅ Sí |
| `provided` | ✅ | ❌ | ✅ | ❌ No |
| `runtime` | ❌ | ✅ | ✅ | ✅ Sí |
| **`test`** | ❌ | ❌ | ✅ | **❌ No** |

**Clave:** `test` scope **NO es transitivo** - por eso no podemos ponerlo solo en `keygo-common`

---

## 💡 ¿Y Lombok?

### Lombok es diferente

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```

**Scope:** `provided` (no `test`)

**¿Poner en keygo-common?**
- ✅ **Sí podría funcionar** para compilación
- ❌ **Pero** cada módulo debe configurar el annotation processor

**Mejor práctica:** Declarar Lombok en cada módulo que lo use, aunque sea redundante

---

## ✅ Estrategia Final Implementada

### La Realidad con Spring Boot Parent

**Spring Boot parent** YA gestiona las versiones de dependencias de test en su `<dependencyManagement>`:
- JUnit Jupiter
- Mockito
- AssertJ
- Y más...

### Por eso NO necesitamos `<dependencyManagement>` en nuestro POM

### Módulos Hijos (ejemplo: keygo-app/pom.xml)
```xml
<dependencies>
    <!-- Las versiones vienen de spring-boot-starter-parent -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
        <!-- NO version - Spring Boot parent la gestiona -->
    </dependency>
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <scope>test</scope>
        <!-- NO version - Spring Boot parent la gestiona -->
    </dependency>
</dependencies>
```

**Clave:** Como heredamos de `spring-boot-starter-parent`, ya tenemos `<dependencyManagement>` con todas las versiones.

---

## 🎯 Ventajas de Esta Estrategia (La Implementada Actualmente)

### 1. ✅ Versiones Centralizadas
- Un solo lugar para actualizar versiones
- Consistencia garantizada entre módulos

### 2. ✅ Explícito
- Cada módulo declara sus dependencias
- Fácil ver qué necesita cada módulo

### 3. ✅ Flexible
- Módulos pueden elegir qué dependencias de test usar
- No todos necesitan Mockito, por ejemplo

### 4. ✅ Mantenible
- Cambios de versión en un solo lugar
- Clear separation of concerns

---

## 📚 Referencias Maven

### DependencyManagement vs Dependencies

**`<dependencyManagement>`:**
- Define versiones disponibles
- NO agrega dependencias al proyecto
- Los hijos pueden usarlas sin especificar versión

**`<dependencies>`:**
- Agrega dependencias al proyecto actual
- Si está en padre, TODOS los hijos las heredan

---

## 🔄 Refactorización Realizada

### Antes (mi primera versión)
```
✅ Funcionaba
⚠️ Duplicación de declaraciones
⚠️ Difícil mantener versiones consistentes
```

### Después (versión mejorada)
```
✅ Funciona igual de bien
✅ Versiones centralizadas en padre
✅ Fácil mantenimiento
✅ Mejor práctica de Maven
```

---

## ✅ Resumen

**Tu pregunta era correcta:**
- ¿Por qué no usar `keygo-common`?
- Respuesta: Porque `test` scope no es transitivo

**Solución correcta:**
- `<dependencyManagement>` en POM padre
- Cada módulo declara dependencias sin versión
- Versiones gestionadas centralmente

**Resultado:**
- ✅ No hay duplicación de versiones
- ✅ Cada módulo declara lo que necesita
- ✅ Fácil mantenimiento
- ✅ Best practice de Maven

---

**Estado:** ✅ Refactorizado correctamente

Ahora el proyecto usa la estrategia correcta de gestión de dependencias Maven.

