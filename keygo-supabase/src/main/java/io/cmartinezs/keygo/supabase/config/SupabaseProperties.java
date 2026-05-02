package io.cmartinezs.keygo.supabase.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * Supabase configuration properties
 * Propiedades de configuración de Supabase
 * <p>
 * All properties are loaded from environment variables defined in .env files
 * Todas las propiedades se cargan desde variables de entorno definidas en archivos .env
 *
 * @author cmartinezs
 * @version 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "supabase")
public class SupabaseProperties {

    /**
     * Database connection URL (JDBC format)
     * URL de conexión a la base de datos (formato JDBC)
     * <p>
     * Environment variable: SUPABASE_URL
     * Example: jdbc:postgresql://localhost:5432/keygo
     */
    private String url;

    /**
     * Database user
     * Usuario de la base de datos
     * <p>
     * Environment variable: SUPABASE_USER
     * Example: postgres
     */
    private String user;

    /**
     * Database password
     * Contraseña de la base de datos
     * <p>
     * Environment variable: SUPABASE_PASSWORD
     */
    private String password;

    /**
     * Supabase project ID
     * ID del proyecto Supabase
     * <p>
     * Environment variable: SUPABASE_PROJECT_ID
     * Example: xxxxxxxxxxxxx
     */
    private String projectId;

    /**
     * Supabase API URL
     * URL de la API de Supabase
     * <p>
     * Environment variable: SUPABASE_API_URL
     * Example: https://xxxxxxxxxxxxx.supabase.co
     */
    private String apiUrl;

    /**
     * Supabase REST API URL
     * URL de la API REST de Supabase
     * <p>
     * Environment variable: SUPABASE_REST_URL
     * Example: https://xxxxxxxxxxxxx.supabase.co/rest/v1
     */
    private String restUrl;

    /**
     * Supabase GraphQL API URL
     * URL de la API GraphQL de Supabase
     * <p>
     * Environment variable: SUPABASE_GRAPHQL_URL
     * Example: https://xxxxxxxxxxxxx.supabase.co/graphql/v1
     */
    private String graphqlUrl;

    /**
     * Supabase Realtime WebSocket URL
     * URL de WebSocket Realtime de Supabase
     * <p>
     * Environment variable: SUPABASE_REALTIME_URL
     * Example: wss://xxxxxxxxxxxxx.supabase.co/realtime/v1
     */
    private String realtimeUrl;

    /**
     * Supabase Storage API URL
     * URL de la API de Storage de Supabase
     * <p>
     * Environment variable: SUPABASE_STORAGE_URL
     * Example: https://xxxxxxxxxxxxx.supabase.co/storage/v1
     */
    private String storageUrl;

    /**
     * Supabase anonymous key (public)
     * Clave anónima de Supabase (pública)
     * <p>
     * Environment variable: SUPABASE_ANON_KEY
     * Safe to expose in client-side code
     */
    private String anonKey;

    /**
     * Supabase service role key (secret)
     * Clave de rol de servicio de Supabase (secreta)
     * <p>
     * Environment variable: SUPABASE_SERVICE_KEY
     * NEVER expose in client-side code!
     */
    private String serviceKey;

    /**
     * JWT secret for token validation
     * Secreto JWT para validación de tokens
     * <p>
     * Environment variable: SUPABASE_JWT_SECRET
     * Keep this secret secure!
     */
    private String jwtSecret;

    /**
     * Database configuration nested properties
     * Propiedades anidadas de configuración de base de datos
     */
    private Db db = new Db();

    /**
     * Database configuration
     * Configuración de base de datos
     */
    @Data
    public static class Db {
        /**
         * Database host
         * Environment variable: SUPABASE_DB_HOST
         */
        private String host;

        /**
         * Database port
         * Environment variable: SUPABASE_DB_PORT
         */
        private Integer port;

        /**
         * Database name
         * Environment variable: SUPABASE_DB_NAME
         */
        private String name;

        /**
         * Database schema
         * Environment variable: SUPABASE_DB_SCHEMA
         */
        private String schema = "public";
    }
}

