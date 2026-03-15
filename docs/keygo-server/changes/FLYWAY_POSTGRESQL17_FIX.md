# Flyway PostgreSQL 17.6 Compatibility Fix

## Problem

The application was failing to start with the following error:

```
org.flywaydb.core.api.FlywayException: Unsupported Database: PostgreSQL 17.6
```

This occurred because Flyway 11.14.1 (included in Spring Boot 4.0.3) did not support PostgreSQL 17.6.

## Solution

Updated Flyway to version 11.20.3, which includes support for PostgreSQL 17.x.

### Changes Made

**File:** `/home/cmartinezs/Github/cmartinezs/keygo-server/pom.xml`

Added the `flyway.version` property to override the default Flyway version managed by Spring Boot:

```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.release>${java.version}</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <flyway.version>11.20.3</flyway.version>
</properties>
```

## Verification

The project was successfully rebuilt with the new Flyway version:

```bash
./mvnw clean install -DskipTests
```

All modules built successfully, confirming that the dependency resolution is working correctly.

## Compatibility

- **Flyway Version:** 11.20.3
- **PostgreSQL Version:** 17.6
- **Spring Boot Version:** 4.0.3
- **Java Version:** 21

## References

- Flyway Maven Central: https://repo.maven.apache.org/maven2/org/flywaydb/flyway-core/
- Flyway Documentation: https://documentation.red-gate.com/flyway

