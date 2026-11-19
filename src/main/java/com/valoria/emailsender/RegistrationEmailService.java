package com.valoria.emailsender;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Service
public class RegistrationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationEmailService.class);

    private final JavaMailSender mailSender;
    private final String senderAddress;
    private final String subject;
    private final String message;

    public RegistrationEmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String senderAddress,
            @Value("${app.mail.subject}") String subject,
            @Value("${app.mail.message}") String message) {
        this.mailSender = mailSender;
        this.senderAddress = senderAddress;
        this.subject = subject;
        this.message = message;
    }
public void sendRegistrationEmail(String recipientAddress) {

    logger.info("Preparing registration email for: {}", recipientAddress);

    SimpleMailMessage email = new SimpleMailMessage();

    email.setFrom(senderAddress);
    email.setTo(recipientAddress);
    email.setSubject(subject);
    email.setText(message);

    try {
        mailSender.send(email);

        logger.info("Registration email sent successfully to: {}",
                recipientAddress);

    } catch (MailException e) {

    logger.error(
        "Registration email failed for recipient: {}",
        recipientAddress,
        e
    );

    throw e;
}
}
}