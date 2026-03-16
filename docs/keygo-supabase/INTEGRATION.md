# KeyGo Supabase Integration Instructions
# Instrucciones de Integración de KeyGo Supabase

## 🎯 Purpose / Propósito

This guide explains how to integrate the keygo-supabase module with the rest of the KeyGo application.

Esta guía explica cómo integrar el módulo keygo-supabase con el resto de la aplicación KeyGo.

---

## 📦 Step 1: Add Dependency to keygo-run / Agregar Dependencia a keygo-run

> ✅ **Ya implementado / Already implemented:** `keygo-run/pom.xml` ya incluye esta dependencia.

Edit `keygo-run/pom.xml` and add the supabase dependency:

```xml
<dependencies>
    <!-- Existing dependencies -->
    
    <!-- KeyGo Supabase -->
    <dependency>
        <groupId>io.cmartinezs.keygo</groupId>
        <artifactId>keygo-supabase</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

---

## ⚙️ Step 2: Configure Application Properties / Configurar Propiedades

### Option A: Using Environment Variables (Recommended)

Create or edit `.env` file in project root:

```bash
# Supabase Configuration
SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo
SUPABASE_USER=postgres
SUPABASE_PASSWORD=postgres
SUPABASE_API_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-anon-key
SUPABASE_SERVICE_KEY=your-service-key
```

### Option B: Using application.yml

Edit `keygo-run/src/main/resources/application.yml`:

```yaml
spring:
  profiles:
    active: supabase  # Activate supabase profile
  
  datasource:
    url: ${SUPABASE_URL:jdbc:postgresql://localhost:5432/keygo}
    username: ${SUPABASE_USER:postgres}
    password: ${SUPABASE_PASSWORD:postgres}
```

---

## 🔧 Step 3: Enable Component Scanning / Habilitar Escaneo de Componentes

> ✅ **Ya implementado / Already implemented:**  
> `ApplicationConfig` ya incluye `"io.cmartinezs.keygo.supabase"` en su `@ComponentScan`.  
> `SupabaseJpaConfig` maneja `@EntityScan` y `@EnableJpaRepositories` automáticamente.

Edit `keygo-run/src/main/java/io/cmartinezs/keygo/run/config/ApplicationConfig.java`:

```java
@Configuration
@ComponentScan(basePackages = {
    "io.cmartinezs.keygo.api",
    "io.cmartinezs.keygo.supabase"  // ✅ Add this line
})
public class ApplicationConfig {
    // ... existing code
}
```

Or create a separate configuration class:

```java
package io.cmartinezs.keygo.run.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "io.cmartinezs.keygo.supabase")
public class SupabaseConfig {
}
```

---

## 🚀 Step 4: Run Database Migrations / Ejecutar Migraciones

### For Local Development / Para Desarrollo Local

```bash
# Start local database
cd keygo-supabase
./scripts/dev-start.sh

# Run migrations
export SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo
export SUPABASE_USER=postgres
export SUPABASE_PASSWORD=postgres
./scripts/migrate.sh
```

### For Production / Para Producción

```bash
# Set production environment
export SUPABASE_URL=postgresql://postgres:[PASSWORD]@db.[PROJECT].supabase.co:5432/postgres
export SUPABASE_USER=postgres
export SUPABASE_PASSWORD=your-production-password

# Run setup
cd keygo-supabase
./scripts/setup-supabase.sh
```

---

## 🏃 Step 5: Run the Application / Ejecutar la Aplicación

```bash
cd keygo-server

# Option 1: With Maven wrapper
./mvnw spring-boot:run -pl keygo-run

# Option 2: After building
./mvnw clean package
java -jar keygo-run/target/keygo-run-1.0-SNAPSHOT.jar

# Option 3: With environment variables
SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo \
SUPABASE_USER=postgres \
SUPABASE_PASSWORD=postgres \
./mvnw spring-boot:run -pl keygo-run
```

---

## ✅ Step 6: Verify Integration / Verificar Integración

### Test Database Connection

> ⚠️ **Nota de arquitectura / Architecture note:** En un entorno hexagonal, `keygo-api` **no debe** depender directamente de `keygo-supabase` ni de sus repositorios JPA. La integración correcta pasa por puertos en `keygo-app`. El ejemplo a continuación es solo para verificación rápida en desarrollo — **no usar en producción**.

Para verificar la conexión a la base de datos, comprueba los logs al arrancar con el perfil `supabase`:

```bash
export SPRING_PROFILES_ACTIVE="supabase,local"
export SUPABASE_URL="jdbc:postgresql://localhost:5432/keygo"
export SUPABASE_USER="postgres"
export SUPABASE_PASSWORD="postgres"
./mvnw spring-boot:run -pl keygo-run
```

Busca en los logs:
```
HikariPool-1 - Start completed.
Successfully applied N migration(s) to schema "public"
```

También puedes usar el actuator para verificar que la app está corriendo:

```bash
curl http://localhost:8080/keygo-server/actuator/health
# Expected: {"status":"UP"}
```

---

## 📝 Step 7: Create Use Case + Port (Hexagonal) / Crear Caso de Uso + Puerto

> ⚠️ **Arquitectura hexagonal:** `keygo-app` **no debe** importar clases de `keygo-supabase` directamente.
> El patrón correcto es definir un puerto OUT en `keygo-app` e implementarlo en `keygo-supabase`.

### 7.1 Puerto OUT en `keygo-app`

```java
// keygo-app/src/main/java/io/cmartinezs/keygo/app/port/out/UserRepositoryPort.java
package io.cmartinezs.keygo.app.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    // Sustituir Object por la entidad de dominio cuando keygo-domain esté implementado
    List<Object> findAll();
    Optional<Object> findById(UUID id);
}
```

### 7.2 Implementación en `keygo-supabase`

```java
// keygo-supabase/src/main/java/io/cmartinezs/keygo/supabase/adapter/UserRepositoryAdapter.java
package io.cmartinezs.keygo.supabase.adapter;

import io.cmartinezs.keygo.app.port.out.UserRepositoryPort;
import io.cmartinezs.keygo.supabase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserRepository jpaRepository;
    // implementar métodos del puerto
}
```

> 💡 Este patrón mantiene `keygo-app` libre de dependencias de infraestructura.

---

## 🔐 Step 8: Add Security Configuration (Optional) / Configuración de Seguridad

If using Spring Security:

```java
package io.cmartinezs.keygo.run.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .build();
    }
}
```

---

## 🧪 Step 9: Run Tests / Ejecutar Pruebas

```bash
# Test the supabase module
./mvnw test -pl keygo-supabase

# Test with all modules
./mvnw clean test
```

---

## 📊 Step 10: Monitor Database / Monitorear Base de Datos

### Using PgAdmin (Local Development)

```bash
# PgAdmin is available at:
http://localhost:5050

# Credentials:
Email: admin@keygo.local
Password: admin
```

### Using Supabase Dashboard (Production)

Visit your Supabase project dashboard:
```
https://app.supabase.com/project/[your-project-id]
```

---

## 🔄 Migration Workflow / Flujo de Migraciones

### Development

1. Make changes to database schema
2. Create new migration file in `keygo-supabase/src/main/resources/db/migration/`
3. Test locally: `./scripts/migrate.sh`
4. Verify: `./scripts/info.sh`
5. Commit migration file

### Production

1. Pull latest code with new migrations
2. Set production environment variables
3. Run: `./scripts/migrate.sh`
4. Verify: `./scripts/info.sh`

---

## 🐛 Troubleshooting / Solución de Problemas

### Problem: Module not found

**Solution:** Make sure the module is added to parent pom.xml:

```xml
<modules>
    <!-- ... -->
    <module>keygo-supabase</module>
</modules>
```

### Problem: Database connection fails

**Solution:** Check environment variables:

```bash
echo $SUPABASE_URL
echo $SUPABASE_USER
# Don't echo password in production!
```

### Problem: Entities not found

**Solution:** Add component scanning:

```java
@ComponentScan(basePackages = "io.cmartinezs.keygo.supabase")
```

### Problem: Migrations fail

**Solution:** Check migration status and repair:

```bash
./scripts/info.sh
./scripts/repair.sh
./scripts/migrate.sh
```

---

## 📚 Additional Resources / Recursos Adicionales

- [README.md](./README.md) - Module documentation
- [MIGRATIONS.md](./MIGRATIONS.md) - Migration guide
- [SUMMARY.md](./SUMMARY.md) - Complete summary
- [Supabase Docs](https://supabase.com/docs)
- [Flyway Docs](https://flywaydb.org/documentation/)

---

## ✅ Checklist / Lista de Verificación

- [x] Add dependency to keygo-run/pom.xml ✅ ya incluido
- [ ] Configure environment variables
- [x] Enable component scanning ✅ ya configurado en ApplicationConfig + SupabaseJpaConfig
- [ ] Start local database
- [ ] Run migrations
- [ ] Start application
- [ ] Test database connection
- [ ] Create use cases + ports + adapters (hexagonal)
- [ ] Run tests

---

**Last Updated:** 2026-03-15
**Version:** 1.0

