package com.maharshi.bollywood_game_spring_boot.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendMail(String to, String subject, String body) {
        try {
            // Create a MimeMessage
            MimeMessage message = javaMailSender.createMimeMessage();

            // Use MimeMessageHelper to construct the message
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // Set the second parameter to true for HTML

            // Send the email
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

