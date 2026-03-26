package io.cmartinezs.keygo.run.config.security;

import tools.jackson.databind.json.JsonMapper;
import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.run.config.properties.KeyGoCorsProperties;
import io.cmartinezs.keygo.run.config.properties.KeyGoBootstrapProperties;
import io.cmartinezs.keygo.run.filter.BootstrapAdminKeyFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public BootstrapAdminKeyFilter bootstrapAdminKeyFilter(
      KeyGoBootstrapProperties bootstrapProperties,
      JsonMapper jsonMapper,
      Environment environment,
      ObjectProvider<AccessTokenVerifierPort> accessTokenVerifier,
      ObjectProvider<SigningKeyRepositoryPort> signingKeyRepository) {
    return new BootstrapAdminKeyFilter(
        bootstrapProperties,
        jsonMapper,
        accessTokenVerifier.getIfAvailable(),
        signingKeyRepository.getIfAvailable(),
        environment.acceptsProfiles(Profiles.of("local", "dev")));
  }

  /**
   * CORS configuration that allows the configured frontend origins to call all API endpoints.
   *
   * <p>Supports credentials (JSESSIONID cookie) required by the OAuth2 authorize → login flow.
   * Configured via {@code keygo.cors.*} in {@code application.yml}.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource(KeyGoCorsProperties corsProperties) {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
    configuration.setAllowedMethods(corsProperties.getAllowedMethods());
    configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
    configuration.setAllowCredentials(corsProperties.isAllowCredentials());
    configuration.setMaxAge(corsProperties.getMaxAge());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      BootstrapAdminKeyFilter bootstrapAdminKeyFilter,
      CorsConfigurationSource corsConfigurationSource) {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .addFilterBefore(bootstrapAdminKeyFilter, AnonymousAuthenticationFilter.class);

    return http.build();
  }
}





