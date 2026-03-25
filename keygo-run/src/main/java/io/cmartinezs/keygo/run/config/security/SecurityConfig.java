package io.cmartinezs.keygo.run.config.security;

import tools.jackson.databind.json.JsonMapper;
import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.run.config.properties.KeyGoBootstrapProperties;
import io.cmartinezs.keygo.run.filter.BootstrapAdminKeyFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public BootstrapAdminKeyFilter bootstrapAdminKeyFilter(
      KeyGoBootstrapProperties bootstrapProperties,
      JsonMapper jsonMapper,
      ObjectProvider<AccessTokenVerifierPort> accessTokenVerifier,
      ObjectProvider<SigningKeyRepositoryPort> signingKeyRepository) {
    return new BootstrapAdminKeyFilter(
        bootstrapProperties,
        jsonMapper,
        accessTokenVerifier.getIfAvailable(),
        signingKeyRepository.getIfAvailable());
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      BootstrapAdminKeyFilter bootstrapAdminKeyFilter) {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .addFilterBefore(bootstrapAdminKeyFilter, AnonymousAuthenticationFilter.class);

    return http.build();
  }
}



