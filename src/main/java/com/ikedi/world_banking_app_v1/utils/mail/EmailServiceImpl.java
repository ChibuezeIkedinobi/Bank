package com.ikedi.world_banking_app_v1.utils.mail;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @PostConstruct
    public void validateSenderEmail() {
        if (senderEmail == null || senderEmail.isBlank()) {
            throw new IllegalStateException("Sender email is not configured properly. Check 'spring.mail.username' in your application properties.");
        }
    }

    @Override
    public void sendEmailAlert(EmailDetails emailDetails) {

        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true); // 'true' enables multipart emails

            helper.setFrom(senderEmail);
            helper.setTo(emailDetails.getRecipient());
            helper.setSubject(emailDetails.getSubject());
            helper.setText(emailDetails.getMessageBody(), false); // 'false' indicates plain text

            // Check and attach files if provided
            if (emailDetails.getAttachmentContent() != null && emailDetails.getAttachmentName() != null) {
                ByteArrayResource attachment = new ByteArrayResource(emailDetails.getAttachmentContent());
                helper.addAttachment(emailDetails.getAttachmentName(), attachment);
            }

            // Send the email
            javaMailSender.send(mimeMessage);
            System.out.println("Email sent successfully to " + emailDetails.getRecipient());

        } catch (MessagingException | MailException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}