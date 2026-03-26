package io.cmartinezs.keygo.run.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA configuration for the "local" profile (H2 in-memory database).
 * Reutiliza las mismas entidades y repositorios de keygo-supabase pero contra H2.
 * Flyway está deshabilitado en este perfil; Hibernate genera el DDL con create-drop.
 *
 * @author cmartinezs
 */
@Configuration
@Profile("local")
@EntityScan(basePackages = "io.cmartinezs.keygo.supabase")
@EnableJpaRepositories(basePackages = "io.cmartinezs.keygo.supabase")
@EnableTransactionManagement
public class LocalJpaConfig {
}

