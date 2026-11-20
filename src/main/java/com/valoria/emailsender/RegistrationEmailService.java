
package com.valoria.emailsender;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class RegistrationEmailService {

    private static final Logger logger =
            LoggerFactory.getLogger(RegistrationEmailService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String senderAddress;
    private final String subject;
    private final String message;
    private final ObjectMapper objectMapper;

    public RegistrationEmailService(
            @Value("${app.email.resend.api-key}") String apiKey,
            @Value("${app.mail.from}") String senderAddress,
            @Value("${app.mail.subject}") String subject,
            @Value("${app.mail.message}") String message,
            @Value("${app.email.resend.api-url:https://api.resend.com}") String apiUrl,
            ObjectMapper objectMapper) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Resend API key is missing");
        }

        if (senderAddress == null || senderAddress.isBlank()) {
            throw new IllegalStateException("Sender email is missing");
        }

        this.apiKey = apiKey;
        this.senderAddress = senderAddress;
        this.subject = subject;
        this.message = message;
        this.objectMapper = objectMapper;

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .build();
    }

    public void sendRegistrationEmail(String recipientAddress) {

        if (recipientAddress == null || recipientAddress.isBlank()) {
            throw new IllegalArgumentException(
                    "Recipient email cannot be empty");
        }

        logger.info("Preparing registration email for: {}",
                recipientAddress);

        Map<String, Object> requestBody = Map.of(
                "from", senderAddress,
                "to", List.of(recipientAddress.trim()),
                "subject", subject,
                "text", message
        );

        try {

            String response = restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            String resendEmailId = "unknown";

            if (response != null) {
                JsonNode responseJson =
                        objectMapper.readTree(response);

                resendEmailId =
                        responseJson.path("id").asText("unknown");
            }

            logger.info(
                    "Resend accepted email. Recipient: {}, Email ID: {}",
                    recipientAddress,
                    resendEmailId
            );

        } catch (RestClientResponseException e) {

            logger.error(
                    "Resend rejected email. HTTP status: {}, Response: {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            throw new IllegalStateException(
                    "Resend API error: " +
                    e.getResponseBodyAsString(), e
            );

        } catch (RestClientException e) {

            logger.error(
                    "Resend connection failed for recipient: {}",
                    recipientAddress,
                    e
            );

            throw new IllegalStateException(
                    "Resend API connection failed: " +
                    e.getMessage(), e
            );

        } catch (Exception e) {

            logger.error("Error processing Resend response", e);

            throw new IllegalStateException(
                    "Could not process Resend response", e
            );
        }
    }
}
