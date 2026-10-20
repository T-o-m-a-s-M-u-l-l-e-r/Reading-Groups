package com.muller_tomas.reading_groups.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.muller_tomas.reading_groups.exception.EmailDeliveryException;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
	@Mock
	private JavaMailSender javaMailSender;

	private EmailService emailService;

	@BeforeEach
	void setUp() {
		emailService = new EmailService(javaMailSender, "Reading Groups <no-reply@example.test>");
	}

	@Test
	void sendsPlainTextMessageWithConfiguredSenderAndRequestedRecipient() {
		emailService.sendEmail("reader@example.test", "Verify your email", "Open this verification link.");

		ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(javaMailSender).send(messageCaptor.capture());
		SimpleMailMessage message = messageCaptor.getValue();
		assertEquals("Reading Groups <no-reply@example.test>", message.getFrom());
		assertEquals("reader@example.test", message.getTo()[0]);
		assertEquals("Verify your email", message.getSubject());
		assertEquals("Open this verification link.", message.getText());
	}

	@Test
	void wrapsSmtpFailuresForAfterCommitHandling() {
		MailSendException cause = new MailSendException("SMTP server unavailable");
		doThrow(cause).when(javaMailSender).send(any(SimpleMailMessage.class));

		EmailDeliveryException exception = assertThrows(EmailDeliveryException.class,
				() -> emailService.sendEmail("reader@example.test", "Verify your email", "Verification link"));
		assertSame(cause, exception.getCause());
	}
}
