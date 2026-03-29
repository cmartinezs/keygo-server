package io.cmartinezs.keygo.run.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the billing module.
 * Reads from keygo.billing.* in application.yml.
 *
 * @author cmartinezs
 * @version 1.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "keygo.billing")
public class KeyGoBillingProperties {

  /** When true, the /mock-approve-payment endpoint is enabled (dev/test only). */
  private boolean mockPaymentEnabled = false;

  /** TTL in hours before a contract expires if not activated. */
  private int contractExpiryHours = 48;

  /** Default currency for new plans. */
  private String defaultCurrency = "MXN";
}

