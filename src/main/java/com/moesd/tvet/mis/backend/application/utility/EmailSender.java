package com.moesd.tvet.mis.backend.application.utility;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.moesd.tvet.mis.backend.application.model.EmailNotification;
import com.moesd.tvet.mis.backend.application.repository.EmailNotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Queues an email in tbl_email_notification_dtls; EmailDispatchJob sends it and retries on failure.
 * Called inside a transaction, the email is only queued if that transaction commits.
 * Never throws, so a mail problem cannot break the action that triggered it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender {

	public static final String TYPE_GENERAL = "GENERAL";

	private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

	private final EmailNotificationRepository emailNotificationRepository;

	/** Queues a plain-text email. @return true when it was queued */
	public boolean send(String to, String subject, String text) {
		return queue(to, subject, text, false, TYPE_GENERAL, null);
	}

	/** Queues an HTML email. @return true when it was queued */
	public boolean sendHtml(String to, String subject, String html) {
		return queue(to, subject, html, true, TYPE_GENERAL, null);
	}

	/**
	 * Queues an email.
	 * @param emailType   e.g. CREDENTIALS, PASSWORD_RESET
	 * @param referenceNo application no / user id it belongs to (optional)
	 * @return true when it was queued (false for an invalid address or empty body)
	 */
	public boolean queue(String to, String subject, String body, boolean html, String emailType,
			String referenceNo) {
		try {
			String recipient = to == null ? null : to.trim();
			LocalDateTime now = LocalDateTime.now();
			EmailNotification email = EmailNotification.builder()
					.emailTo(recipient)
					.subject(subject == null ? "" : subject)
					.body(body)
					.html(html)
					.emailType(emailType == null ? TYPE_GENERAL : emailType)
					.referenceNo(referenceNo)
					.attempts(0)
					.createdAt(now)
					.nextAttemptAt(now)
					.build();

			// kept as FAILED (with the reason) so it is visible, but never sent
			String invalid = (recipient == null || !EMAIL.matcher(recipient).matches())
					? "Invalid email address: " + to
					: (body == null || body.isBlank()) ? "Empty body" : null;
			if (invalid != null) {
				email.setStatus(EmailNotification.FAILED);
				email.setLastError(invalid);
				email.setBody(NotificationMasker.mask(body));
				emailNotificationRepository.save(email);
				log.warn("Email not queued ({}): {}", emailType, invalid);
				return false;
			}

			email.setStatus(EmailNotification.PENDING);
			emailNotificationRepository.save(email);
			log.info("Email '{}' queued ({}) for {}", email.getSubject(), email.getEmailType(), recipient);
			return true;
		} catch (Exception e) {
			log.error("Failed to queue email ({}) for {}", emailType, to, e);
			return false;
		}
	}
}
