package io.cmartinezs.keygo.run.config.security;

import io.cmartinezs.keygo.api.error.ApiErrorDataFactory;
import io.cmartinezs.keygo.api.error.ErrorData;
import io.cmartinezs.keygo.api.shared.ResponseCode;
import io.cmartinezs.keygo.api.shared.ResponseHelper;
import io.cmartinezs.keygo.api.shared.response.BaseResponse;
import io.cmartinezs.keygo.app.auth.port.AccessTokenVerifierPort;
import io.cmartinezs.keygo.app.auth.port.SigningKeyRepositoryPort;
import io.cmartinezs.keygo.run.config.properties.KeyGoBootstrapProperties;
import io.cmartinezs.keygo.run.config.properties.KeyGoCorsProperties;
import io.cmartinezs.keygo.run.filter.BootstrapAdminKeyFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.json.JsonMapper;

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
   * Custom AccessDeniedHandler that writes a structured BaseResponse JSON with 403.
   * Acts as fallback when ExceptionTranslationFilter intercepts AccessDeniedException
   * before Spring MVC exception resolvers can handle it.
   * <p>Handler de acceso denegado que escribe un JSON BaseResponse con 403.
   */
  @Bean
  public AccessDeniedHandler keyGoAccessDeniedHandler(JsonMapper jsonMapper, Environment environment) {
    boolean includeTechnicalDetails = environment.acceptsProfiles(Profiles.of("local", "dev"));
    return (request, response, accessDeniedException) -> {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");
      BaseResponse<ErrorData> body = BaseResponse.<ErrorData>builder()
          .failure(ResponseHelper.message(ResponseCode.INSUFFICIENT_PERMISSIONS))
          .data(ApiErrorDataFactory.fromException(
              ResponseCode.INSUFFICIENT_PERMISSIONS, accessDeniedException, includeTechnicalDetails))
          .build();
      jsonMapper.writeValue(response.getWriter(), body);
    };
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
      CorsConfigurationSource corsConfigurationSource,
      AccessDeniedHandler keyGoAccessDeniedHandler) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .headers(headers -> headers
            .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
        .exceptionHandling(ex -> ex.accessDeniedHandler(keyGoAccessDeniedHandler))
        .addFilterBefore(bootstrapAdminKeyFilter, AnonymousAuthenticationFilter.class);

    return http.build();
  }
}





