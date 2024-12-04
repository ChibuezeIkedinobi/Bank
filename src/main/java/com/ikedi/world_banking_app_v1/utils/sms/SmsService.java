package com.ikedi.world_banking_app_v1.utils.sms;

import com.vonage.client.VonageClient;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsService {


        @Value("${vonage.api-key}")
        private String apiKey;

        @Value("${vonage.api-secret}")
        private String apiSecret;

        public String sendSms(String recipientPhoneNumber, String messageContent) {
            try {
                // Initialize Vonage client
                VonageClient vonageClient = VonageClient.builder()
                        .apiKey(apiKey)
                        .apiSecret(apiSecret)
                        .build();

                // Prepare the message
                TextMessage message = new TextMessage("KakiBank", recipientPhoneNumber, messageContent);

                // Send the message
                SmsSubmissionResponse response = vonageClient.getSmsClient().submitMessage(message);

                if (response.getMessages().get(0).getStatus() == com.vonage.client.sms.MessageStatus.OK) {
                    return "SMS sent successfully to " + recipientPhoneNumber;
                } else {
                    return "Failed to send SMS: " + response.getMessages().get(0).getErrorText();
                }

            } catch (Exception e) {
                throw new RuntimeException("Error sending SMS: " + e.getMessage(), e);
            }
        }
    }
