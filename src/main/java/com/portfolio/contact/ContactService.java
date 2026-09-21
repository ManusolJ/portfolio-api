package com.portfolio.contact;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Forwards contact-form submissions to the portfolio inbox over SMTP. */
@Service
public class ContactService {

    private final JavaMailSender mailSender;
    private final ContactProperties contactProperties;

    public ContactService(JavaMailSender mailSender, ContactProperties contactProperties) {
        this.mailSender = mailSender;
        this.contactProperties = contactProperties;
    }

    public void sendContactEmail(ContactRequestDto request) {
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setReplyTo(request.email());
        mail.setFrom(contactProperties.from());
        mail.setTo(contactProperties.contactTo());
        mail.setSubject("Portfolio Web - Contact: " + request.subject());
        mail.setText("From: " + request.name() + " <" + request.email() + ">\n\n" + request.message());

        mailSender.send(mail);
    }
}
