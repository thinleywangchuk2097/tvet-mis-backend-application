package com.moesd.tvet.mis.backend.application.utility;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.moesd.tvet.mis.backend.application.model.SmsNotification;
import com.moesd.tvet.mis.backend.application.repository.SmsNotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Sends queued SMS (tbl_sms_notification_dtls) every minute.
 * A failed SMS is retried after 1, 5, 15 and 30 minutes; after app.sms.max-attempts it is marked FAILED.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsDispatchJob {

	/** minutes to wait before the 2nd, 3rd, 4th, 5th ... attempt */
	private static final int[] RETRY_MINUTES = { 1, 5, 15, 30 };

	private final SmsNotificationRepository smsNotificationRepository;
	private final SmsGatewayClient smsGatewayClient;

	@Value("${app.sms.enabled:true}")
	private boolean enabled;

	@Value("${app.sms.max-attempts:5}")
	private int maxAttempts;

	@Scheduled(fixedDelayString = "${app.sms.dispatch-interval-ms:60000}", initialDelay = 30000)
	public void dispatch() {
		if (!enabled)
			return; // queued SMS stay PENDING until sending is enabled again
		List<SmsNotification> due;
		try {
			due = smsNotificationRepository
					.findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(SmsNotification.PENDING,
							LocalDateTime.now());
		} catch (Exception e) {
			log.error("Could not read queued SMS", e);
			return;
		}
		for (SmsNotification sms : due) {
			try {
				sendOne(sms);
			} catch (Exception e) {
				log.error("SMS {} could not be processed", sms.getId(), e);
			}
		}
	}

	private void sendOne(SmsNotification sms) {
		SmsGatewayClient.Result result = smsGatewayClient.push(sms.getMobileNo(), sms.getMessage());
		int attempts = (sms.getAttempts() == null ? 0 : sms.getAttempts()) + 1;
		LocalDateTime now = LocalDateTime.now();

		sms.setAttempts(attempts);
		sms.setGatewayResponse(result.response());
		sms.setUpdatedAt(now);

		if (result.success()) {
			sms.setStatus(SmsNotification.SENT);
			sms.setSentAt(now);
			sms.setMessage(NotificationMasker.mask(sms.getMessage()));
			log.info("SMS {} ({}) sent to {} after {} attempt(s): {}", sms.getId(), sms.getSmsType(),
					sms.getMobileNo(), attempts, result.response());
		} else if (attempts >= maxAttempts) {
			sms.setStatus(SmsNotification.FAILED);
			sms.setMessage(NotificationMasker.mask(sms.getMessage()));
			log.warn("SMS {} ({}) to {} FAILED after {} attempts: {}", sms.getId(), sms.getSmsType(),
					sms.getMobileNo(), attempts, result.response());
		} else {
			int wait = RETRY_MINUTES[Math.min(attempts - 1, RETRY_MINUTES.length - 1)];
			sms.setNextAttemptAt(now.plusMinutes(wait));
			log.warn("SMS {} ({}) to {} failed (attempt {}), retry in {} min: {}", sms.getId(),
					sms.getSmsType(), sms.getMobileNo(), attempts, wait, result.response());
		}
		smsNotificationRepository.save(sms);
	}
}
