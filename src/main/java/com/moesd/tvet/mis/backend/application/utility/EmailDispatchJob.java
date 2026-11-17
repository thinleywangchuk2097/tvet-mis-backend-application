package com.moesd.tvet.mis.backend.application.utility;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.moesd.tvet.mis.backend.application.model.EmailNotification;
import com.moesd.tvet.mis.backend.application.repository.EmailNotificationRepository;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends queued emails (tbl_email_notification_dtls) through the SMTP server in spring.mail.*.
 * A failed email is retried after 1, 5, 15 and 30 minutes; after app.mail.max-attempts it is marked FAILED.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailDispatchJob {

	/** minutes to wait before the 2nd, 3rd, 4th, 5th ... attempt */
	private static final int[] RETRY_MINUTES = { 1, 5, 15, 30 };

	private final EmailNotificationRepository emailNotificationRepository;
	private final JavaMailSender mailSender;

	@Value("${app.mail.from:no-reply@tvetmis.gov.bt}")
	private String from;

	@Value("${app.mail.enabled:true}")
	private boolean enabled;

	@Value("${app.mail.max-attempts:5}")
	private int maxAttempts;

	@Scheduled(fixedDelayString = "${app.mail.dispatch-interval-ms:60000}", initialDelay = 30000)
	public void dispatch() {
		if (!enabled)
			return; // queued emails stay PENDING until sending is enabled again
		List<EmailNotification> due;
		try {
			due = emailNotificationRepository
					.findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(EmailNotification.PENDING,
							LocalDateTime.now());
		} catch (Exception e) {
			log.error("Could not read queued emails", e);
			return;
		}
		for (EmailNotification email : due) {
			try {
				sendOne(email);
			} catch (Exception e) {
				log.error("Email {} could not be processed", email.getId(), e);
			}
		}
	}

	private void sendOne(EmailNotification email) {
		String error = null;
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
			helper.setFrom(from);
			helper.setTo(email.getEmailTo());
			helper.setSubject(email.getSubject());
			helper.setText(email.getBody(), Boolean.TRUE.equals(email.getHtml()));
			mailSender.send(message);
		} catch (Exception e) {
			error = e.getClass().getSimpleName() + ": " + e.getMessage();
		}

		int attempts = (email.getAttempts() == null ? 0 : email.getAttempts()) + 1;
		LocalDateTime now = LocalDateTime.now();
		email.setAttempts(attempts);
		email.setUpdatedAt(now);
		email.setLastError(error);

		if (error == null) {
			email.setStatus(EmailNotification.SENT);
			email.setSentAt(now);
			email.setBody(NotificationMasker.mask(email.getBody()));
			log.info("Email {} '{}' ({}) sent to {} after {} attempt(s)", email.getId(), email.getSubject(),
					email.getEmailType(), email.getEmailTo(), attempts);
		} else if (attempts >= maxAttempts) {
			email.setStatus(EmailNotification.FAILED);
			email.setBody(NotificationMasker.mask(email.getBody()));
			log.warn("Email {} ({}) to {} FAILED after {} attempts: {}", email.getId(), email.getEmailType(),
					email.getEmailTo(), attempts, error);
		} else {
			int wait = RETRY_MINUTES[Math.min(attempts - 1, RETRY_MINUTES.length - 1)];
			email.setNextAttemptAt(now.plusMinutes(wait));
			log.warn("Email {} ({}) to {} failed (attempt {}), retry in {} min: {}", email.getId(),
					email.getEmailType(), email.getEmailTo(), attempts, wait, error);
		}
		emailNotificationRepository.save(email);
	}
}
