
package com.valoria.emailsender;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/webhooks/supabase")
public class SupabaseWebhookController {

    private static final Logger logger =
            LoggerFactory.getLogger(SupabaseWebhookController.class);

    private static final String STUDENTS_TABLE = "students";

    private final RegistrationEmailService emailService;
    private final byte[] expectedSecret;

    public SupabaseWebhookController(
            RegistrationEmailService emailService,
            @Value("${app.webhook.secret}") String webhookSecret) {

        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException(
                    "Supabase webhook secret must not be blank");
        }

        this.emailService = emailService;
        this.expectedSecret =
                webhookSecret.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/students")
    public ResponseEntity<Void> handleStudentChange(
            @RequestHeader(
                name = "X-Webhook-Secret",
                required = false
            ) String suppliedSecret,
            @RequestBody JsonNode payload) {

        if (!isValidSecret(suppliedSecret)) {
            logger.warn("Rejected webhook: invalid secret");

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid webhook secret");
        }

        String eventType = payload.path("type").asText();
        String table = payload.path("table").asText();

        logger.info(
                "Received Supabase webhook: type={}, table={}",
                eventType, table);

        if (!"INSERT".equalsIgnoreCase(eventType)
                || !STUDENTS_TABLE.equalsIgnoreCase(table)) {

            logger.info("Ignoring unsupported webhook event");

            return ResponseEntity.noContent().build();
        }

        JsonNode emailNode =
                payload.path("record").path("email");

        if (!emailNode.isTextual()
                || emailNode.asText().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Inserted student has no valid email");
        }

        String email = emailNode.asText().trim();

        logger.info("Sending registration email to: {}", email);

        emailService.sendRegistrationEmail(email);

        logger.info(
                "Registration email request processed for: {}",
                email);

        return ResponseEntity.accepted().build();
    }

    private boolean isValidSecret(String suppliedSecret) {

        return suppliedSecret != null
                && MessageDigest.isEqual(
                    expectedSecret,
                    suppliedSecret.getBytes(StandardCharsets.UTF_8));
    }
}
