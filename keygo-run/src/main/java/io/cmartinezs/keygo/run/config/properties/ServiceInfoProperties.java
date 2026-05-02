package io.cmartinezs.keygo.run.config.properties;

import io.cmartinezs.keygo.app.platform.port.ServiceInfoProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for service information from application.yml Propiedades de
 * configuración para información del servicio desde application.yml
 *
 * @author cmartinezs
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "keygo.info")
@Getter
@Setter
public class ServiceInfoProperties implements ServiceInfoProvider {
  private String title;
  private String name;
  private String version;

  @Autowired
  private Environment springEnvironment;

  @Override
  public String getEnvironment() {
    String[] profiles = springEnvironment.getActiveProfiles();
    return profiles.length > 0 ? String.join(",", profiles) : "default";
  }

  @Override
  public String getStatus() {
    return "UP";
  }
}
