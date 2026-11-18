package com.moesd.tvet.mis.backend.application.utility;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.moesd.tvet.mis.backend.application.model.SmsNotification;
import com.moesd.tvet.mis.backend.application.repository.SmsNotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Queues an SMS in tbl_sms_notification_dtls; SmsDispatchJob sends it and retries on failure.
 * Called inside a transaction, the SMS is only queued if that transaction commits
 * (e.g. credentials are only sent when the approval is really saved).
 * Never throws, so an SMS problem cannot break the action that triggered it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsSender {

	public static final String TYPE_GENERAL = "GENERAL";

	private final SmsNotificationRepository smsNotificationRepository;

	/** Queues a general SMS. @return true when it was queued */
	public boolean send(String mobileNo, String message) {
		return send(mobileNo, message, TYPE_GENERAL, null);
	}

	/**
	 * Queues an SMS.
	 * @param smsType     e.g. CREDENTIALS, PASSWORD_RESET
	 * @param referenceNo application no / user id it belongs to (optional)
	 * @return true when it was queued (false for an invalid number or empty message)
	 */
	public boolean send(String mobileNo, String message, String smsType, String referenceNo) {
		try {
			String mobile = SmsGatewayClient.normalizeMobile(mobileNo);
			LocalDateTime now = LocalDateTime.now();
			SmsNotification sms = SmsNotification.builder()
					.mobileNo(mobile != null ? mobile : mobileNo)
					.message(message)
					.smsType(smsType == null ? TYPE_GENERAL : smsType)
					.referenceNo(referenceNo)
					.attempts(0)
					.createdAt(now)
					.nextAttemptAt(now)
					.build();

			// kept as FAILED (with the reason) so it is visible, but never sent
			String invalid = invalidReason(mobile, mobileNo, message);
			if (invalid != null) {
				sms.setStatus(SmsNotification.FAILED);
				sms.setGatewayResponse(invalid);
				sms.setMessage(NotificationMasker.mask(message));
				smsNotificationRepository.save(sms);
				log.warn("SMS not queued ({}): {}", smsType, invalid);
				return false;
			}

			sms.setStatus(SmsNotification.PENDING);
			smsNotificationRepository.save(sms);
			log.info("SMS queued ({}) for {}", sms.getSmsType(), mobile);
			return true;
		} catch (Exception e) {
			log.error("Failed to queue SMS ({}) for {}", smsType, mobileNo, e);
			return false;
		}
	}

	/** Why the SMS cannot be sent (bad number or empty message), or null when it is fine. */
	private static String invalidReason(String mobile, String mobileNo, String message) {
		if (mobile == null)
			return "Invalid mobile number: " + mobileNo;
		if (message == null || message.isBlank())
			return "Empty message";
		return null;
	}
}
