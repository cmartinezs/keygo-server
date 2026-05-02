package io.cmartinezs.keygo.supabase.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Supabase JPA configuration — only active when profile "supabase" is enabled.
 * Configuración de JPA para Supabase — solo activa con el perfil "supabase".
 *
 * @author cmartinezs
 * @version 1.0
 */
@Configuration
@Profile("supabase")
@EntityScan(basePackages = "io.cmartinezs.keygo.supabase")
@EnableJpaRepositories(basePackages = "io.cmartinezs.keygo.supabase")
@EnableTransactionManagement
public class SupabaseJpaConfig {
}


