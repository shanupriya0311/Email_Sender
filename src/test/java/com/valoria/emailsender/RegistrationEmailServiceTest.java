
package com.valoria.emailsender;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RegistrationEmailServiceTest {

    @Autowired
    private RegistrationEmailService emailService;

    @Test
    void sendsRealRegistrationEmail() {

        emailService.sendRegistrationEmail(
            "717823s148@kce.ac.in"
        );
    }
}
