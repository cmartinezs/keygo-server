package io.cmartinezs.keygo.api.test;

import io.cmartinezs.keygo.app.user.port.EmailNotificationPort;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
@RequestMapping("/local/mail")
public class LocalMailTestController {

  private final EmailNotificationPort emailNotificationPort;

  public LocalMailTestController(EmailNotificationPort emailNotificationPort) {
    this.emailNotificationPort = emailNotificationPort;
  }

  @PostMapping("/test")
  public String sendTestMail(
      @RequestParam(defaultValue = EmailNotificationPort.TYPE_EMAIL_VERIFICATION) String type,
      @RequestParam(defaultValue = "demo@keygo.local") String to,
      @RequestParam(defaultValue = "Demo User") String name) {

    emailNotificationPort.sendEmail(type, to, name, Map.of(
        "code", "123456",
        "actionUrl", "http://localhost:5173/verify?token=local-demo-token"
    ));

    return "Email '" + type + "' enviado a " + to;
  }
}
