package com.valoria.emailsender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class SupabaseWebhookControllerTest {

    private final RegistrationEmailService emailService =
            org.mockito.Mockito.mock(RegistrationEmailService.class);
    private final SupabaseWebhookController controller =
            new SupabaseWebhookController(emailService, "test-secret");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void sendsEmailForStudentInsert() throws Exception {
        var payload = objectMapper.readTree("""
                {"type":"INSERT","table":"Students","record":{"email":"student@example.com"}}
                """);

        var response = controller.handleStudentChange("test-secret", payload);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(emailService).sendRegistrationEmail("student@example.com");
    }

    @Test
    void rejectsRequestsWithoutTheCorrectSecret() throws Exception {
        var payload = objectMapper.readTree("""
                {"type":"INSERT","table":"students","record":{"email":"student@example.com"}}
                """);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.handleStudentChange("wrong-secret", payload));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        verifyNoInteractions(emailService);
    }

    @Test
    void ignoresEventsThatAreNotStudentInserts() throws Exception {
        var payload = objectMapper.readTree("""
                {"type":"UPDATE","table":"students","record":{"email":"student@example.com"}}
                """);

        var response = controller.handleStudentChange("test-secret", payload);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verifyNoInteractions(emailService);
    }
}
