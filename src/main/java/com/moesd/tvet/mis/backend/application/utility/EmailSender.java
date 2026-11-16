package com.moesd.tvet.mis.backend.application.utility;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends email through the SMTP server configured in spring.mail.*.
 * Failures are logged and reported as false; they never throw, so a mail problem
 * cannot break the action that triggered it (same rule as SmsSender).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

	private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	private final JavaMailSender mailSender;

	@Value("${app.mail.from:no-reply@tvetmis.gov.bt}")
	private String from;

	@Value("${app.mail.enabled:true}")
	private boolean enabled;

	/** Plain-text email. @return true when the SMTP server accepted it */
	public boolean send(String to, String subject, String text) {
		return deliver(to, subject, text, false);
	}

	/** HTML email (body is HTML). @return true when the SMTP server accepted it */
	public boolean sendHtml(String to, String subject, String html) {
		return deliver(to, subject, html, true);
	}

	private boolean deliver(String to, String subject, String body, boolean html) {
		String recipient = to == null ? null : to.trim();
		if (!enabled) {
			log.info("Email disabled; not sending '{}' to {}", subject, recipient);
			return false;
		}
		if (recipient == null || !EMAIL.matcher(recipient).matches()) {
			log.warn("Email not sent: invalid address '{}'", to);
			return false;
		}
		if (body == null || body.isBlank()) {
			log.warn("Email '{}' to {} not sent: empty body", subject, recipient);
			return false;
		}
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
			helper.setFrom(from);
			helper.setTo(recipient);
			helper.setSubject(subject == null ? "" : subject);
			helper.setText(body, html);
			mailSender.send(message);
			log.info("Email '{}' sent to {}", subject, recipient);
			return true;
		} catch (Exception e) {
			log.error("Failed to send email '{}' to {}: {}", subject, recipient, e.getMessage());
			return false;
		}
	}
}
