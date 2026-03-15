# KeyGo Supabase Integration Instructions
# Instrucciones de Integración de KeyGo Supabase

## 🎯 Purpose / Propósito

This guide explains how to integrate the keygo-supabase module with the rest of the KeyGo application.

Esta guía explica cómo integrar el módulo keygo-supabase con el resto de la aplicación KeyGo.

---

## 📦 Step 1: Add Dependency to keygo-run / Agregar Dependencia a keygo-run

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

Create a test endpoint in `keygo-api`:

```java
package io.cmartinezs.keygo.api.controller;

import io.cmartinezs.keygo.supabase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {
    
    private final UserRepository userRepository;
    
    @GetMapping("/users/count")
    public long getUserCount() {
        return userRepository.count();
    }
}
```

Test the endpoint:

```bash
curl http://localhost:8080/api/test/users/count
# Expected: 1 (the admin user)
```

---

## 📝 Step 7: Create User Service (Optional) / Crear Servicio de Usuario

Create a service layer in `keygo-app`:

```java
package io.cmartinezs.keygo.app.service;

import io.cmartinezs.keygo.supabase.entity.UserEntity;
import io.cmartinezs.keygo.supabase.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<UserEntity> findUserById(UUID id) {
        return userRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<UserEntity> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Transactional
    public UserEntity createUser(UserEntity user) {
        return userRepository.save(user);
    }
}
```

---

## 🔐 Step 8: Add Security Configuration (Optional) / Configuración de Seguridad

If using Spring Security:

```java
package io.cmartinezs.keygo.run.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
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

- [ ] Add dependency to keygo-run/pom.xml
- [ ] Configure environment variables
- [ ] Enable component scanning
- [ ] Start local database
- [ ] Run migrations
- [ ] Start application
- [ ] Test database connection
- [ ] Create service layer (optional)
- [ ] Add security configuration (optional)
- [ ] Run tests

---

**Last Updated:** 2026-03-15
**Version:** 1.0

