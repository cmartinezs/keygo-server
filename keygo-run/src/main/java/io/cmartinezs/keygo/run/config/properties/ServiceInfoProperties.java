package io.cmartinezs.keygo.run.config.properties;

import io.cmartinezs.keygo.app.port.out.ServiceInfoProvider;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for service information from application.yml Propiedades de
 * configuración para información del servicio desde application.yml
 *
 * @author cmartinezs
 * @version 1.0
 */
@Component
@ConfigurationProperties(prefix = "key-go-server.info")
@Getter
@Setter
public class ServiceInfoProperties implements ServiceInfoProvider {
  private String title;
  private String name;
  private String version;
}
