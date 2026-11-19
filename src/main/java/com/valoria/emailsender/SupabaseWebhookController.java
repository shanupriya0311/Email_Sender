package com.valoria.emailsender;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/webhooks/supabase")
public class SupabaseWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseWebhookController.class);
    private static final String STUDENTS_TABLE = "students";

    private final RegistrationEmailService emailService;
    private final byte[] expectedSecret;

    public SupabaseWebhookController(
            RegistrationEmailService emailService,
            @Value("${app.webhook.secret}") String webhookSecret) {
        this.emailService = emailService;
        this.expectedSecret = webhookSecret.getBytes(StandardCharsets.UTF_8);
        if (webhookSecret.isBlank()) {
            throw new IllegalStateException("SUPABASE_WEBHOOK_SECRET must not be blank");
        }
    }

    @PostMapping("/students")
    public ResponseEntity<Void> handleStudentChange(
            @RequestHeader(name = "X-Webhook-Secret", required = false) String suppliedSecret,
            @RequestBody JsonNode payload) {
        if (!isValidSecret(suppliedSecret)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid webhook secret");
        }

        String eventType = payload.path("type").asText();
        String table = payload.path("table").asText();
        logger.info("Received Supabase webhook: type={}, table={}", eventType, table);

        if (!"INSERT".equalsIgnoreCase(eventType)
                || !STUDENTS_TABLE.equalsIgnoreCase(table)) {
            return ResponseEntity.noContent().build();
        }

        JsonNode emailNode = payload.path("record").path("email");
        if (!emailNode.isTextual() || emailNode.asText().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inserted student has no email");
        }

        emailService.sendRegistrationEmail(emailNode.asText());
        logger.info("Registration email delivery completed for Supabase student insert");
        return ResponseEntity.accepted().build();
    }

    private boolean isValidSecret(String suppliedSecret) {
        return suppliedSecret != null
                && MessageDigest.isEqual(
                        expectedSecret, suppliedSecret.getBytes(StandardCharsets.UTF_8));
    }
}
