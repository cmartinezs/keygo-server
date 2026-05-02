package io.cmartinezs.keygo.run.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration for the "local" profile (H2 file-based database).
 * Reutiliza las mismas entidades y repositorios de keygo-supabase pero contra H2.
 * Flyway está deshabilitado en este perfil; Hibernate genera el DDL con create-drop.
 * <p>
 * Solo activa cuando el perfil "local" está presente y el perfil "supabase" NO lo está.
 * Cuando ambos perfiles están activos (local,supabase), SupabaseJpaConfig ya cubre
 * el escaneo de entidades y repositorios, evitando el registro duplicado de beans.
 *
 * @author cmartinezs
 */
@Configuration
@Profile("local & !supabase")
@EntityScan(basePackages = "io.cmartinezs.keygo.supabase")
@EnableJpaRepositories(basePackages = "io.cmartinezs.keygo.supabase")
@EnableTransactionManagement
public class LocalJpaConfig {
}

