package io.cmartinezs.keygo.infra.mail;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import org.junit.jupiter.api.Test;

class SendEmailCommandTest {

  @Test
  void builder_shouldCreateCommandWithDefaults() {
    SendEmailCommand command = SendEmailCommand.builder()
        .emailType("test-type")
        .recipientEmail("test@example.com")
        .build();

    assertEquals("test-type", command.getEmailType());
    assertEquals("test@example.com", command.getRecipientEmail());
    assertNotNull(command.getVariables());
    assertTrue(command.getVariables().isEmpty());
    assertNotNull(command.getPathVariables());
    assertTrue(command.getPathVariables().isEmpty());
    assertNotNull(command.getQueryParams());
    assertTrue(command.getQueryParams().isEmpty());
  }

  @Test
  void withVariable_shouldAddVariableAndReturnThis() {
    SendEmailCommand command = SendEmailCommand.builder()
        .emailType("test-type")
        .recipientEmail("test@example.com")
        .build();

    SendEmailCommand result = command.withVariable("key", "value");

    assertSame(command, result);
    assertEquals("value", command.getVariables().get("key"));
  }

  @Test
  void withPathVariable_shouldAddPathVariableAndReturnThis() {
    SendEmailCommand command = SendEmailCommand.builder()
        .emailType("test-type")
        .recipientEmail("test@example.com")
        .build();

    SendEmailCommand result = command.withPathVariable("pathKey", "pathValue");

    assertSame(command, result);
    assertEquals("pathValue", command.getPathVariables().get("pathKey"));
  }

  @Test
  void withQueryParam_shouldAddQueryParamAndReturnThis() {
    SendEmailCommand command = SendEmailCommand.builder()
        .emailType("test-type")
        .recipientEmail("test@example.com")
        .build();

    SendEmailCommand result = command.withQueryParam("queryKey", "queryValue");

    assertSame(command, result);
    assertEquals("queryValue", command.getQueryParams().get("queryKey"));
  }

  @Test
  void noArgsConstructor_shouldInitializeWithEmptyMaps() {
    SendEmailCommand command = new SendEmailCommand();

    assertNotNull(command.getVariables());
    assertNotNull(command.getPathVariables());
    assertNotNull(command.getQueryParams());
  }

  @Test
  void allArgsConstructor_shouldInitializeAllFields() {
    SendEmailCommand command = new SendEmailCommand(
        "type", "email@test.com", "Name", new HashMap<>(), new HashMap<>(), new HashMap<>()
    );

    assertEquals("type", command.getEmailType());
    assertEquals("email@test.com", command.getRecipientEmail());
    assertEquals("Name", command.getRecipientName());
  }
}