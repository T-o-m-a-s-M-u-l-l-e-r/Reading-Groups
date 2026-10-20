package com.muller_tomas.reading_groups.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.muller_tomas.reading_groups.exception.EmailDeliveryException;

@Service
public class EmailService {
	private final JavaMailSender javaMailSender;
	private final String senderAddress;

	public EmailService(JavaMailSender javaMailSender, @Value("${app.email.from}") String senderAddress) {
		this.javaMailSender = javaMailSender;
		this.senderAddress = senderAddress;
	}

	public void sendEmail(String recipientAddress, String subject, String body) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(senderAddress);
		message.setTo(recipientAddress);
		message.setSubject(subject);
		message.setText(body);

		try {
			javaMailSender.send(message);
		} catch (MailException exception) {
			throw new EmailDeliveryException("Email delivery through SMTP failed", exception);
		}
	}
}
