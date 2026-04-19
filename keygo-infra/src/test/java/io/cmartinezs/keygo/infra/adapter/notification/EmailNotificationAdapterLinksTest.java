package io.cmartinezs.keygo.infra.adapter.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.cmartinezs.keygo.infra.config.KeyGoEmailProperties;
import io.cmartinezs.keygo.infra.config.KeyGoUiProperties;
import io.cmartinezs.keygo.infra.config.KeyGoUiProperties.UiPath;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

class EmailNotificationAdapterLinksTest {

  private TemplateEngine templateEngine;
  private JavaMailSender mailSender;
  private KeyGoUiProperties uiProperties;
  private KeyGoEmailProperties emailProperties;

  @BeforeEach
  void setUp() {
    templateEngine = mock(TemplateEngine.class);
    mailSender = mock(JavaMailSender.class);
    uiProperties = new KeyGoUiProperties();
    emailProperties = new KeyGoEmailProperties();
  }

  private EmailNotificationAdapter createAdapter() {
    return new EmailNotificationAdapter(templateEngine, mailSender, uiProperties, emailProperties);
  }

  @Test
  void generateLink_shouldBuildUriWithPathVariable() {
    UiPath uiPath = new UiPath();
    uiPath.setRoute("/reset-password");
    uiPath.setQueryParams(null);
    uiProperties.setPaths(Map.of("password-reset", uiPath));

    EmailNotificationAdapter adapter = createAdapter();

    Map<String, Object> variables = Map.of("token", "abc123");

    String result = invokeGenerateLink(adapter, uiPath, variables);

    assertNotNull(result);
    assertTrue(result.contains("/reset-password"));
  }

  @Test
  void generateLink_shouldAddQueryParams() {
    UiPath uiPath = new UiPath();
    uiPath.setRoute("/verify");
    uiPath.setQueryParams(java.util.List.of("token", "userId"));
    uiProperties.setPaths(Map.of("verify", uiPath));

    EmailNotificationAdapter adapter = createAdapter();

    Map<String, Object> variables = Map.of("token", "t1", "userId", "u1");

    String result = invokeGenerateLink(adapter, uiPath, variables);

    assertNotNull(result);
    assertTrue(result.contains("token="));
    assertTrue(result.contains("userId="));
  }

  private String invokeGenerateLink(EmailNotificationAdapter adapter, KeyGoUiProperties.UiPath uiPath, Map<String, Object> variables) {
    try {
      var method = EmailNotificationAdapter.class.getDeclaredMethod("generateLink", KeyGoUiProperties.UiPath.class, Map.class);
      method.setAccessible(true);
      return (String) method.invoke(adapter, uiPath, variables);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}