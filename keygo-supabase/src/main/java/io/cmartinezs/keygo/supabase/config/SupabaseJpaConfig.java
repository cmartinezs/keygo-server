package io.cmartinezs.keygo.supabase.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Supabase JPA configuration
 * Configuración de JPA para Supabase
 *
 * @author cmartinezs
 * @version 1.0
 */
@Configuration
@EntityScan(basePackages = "io.cmartinezs.keygo.supabase")
@EnableJpaRepositories(basePackages = "io.cmartinezs.keygo.supabase")
@EnableTransactionManagement
public class SupabaseJpaConfig {
}


