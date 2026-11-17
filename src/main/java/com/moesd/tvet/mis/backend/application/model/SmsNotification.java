package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Every SMS the system sends: queued as PENDING, sent by SmsDispatchJob,
 * retried on failure, then marked SENT or FAILED.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_sms_notification_dtls", indexes = {
		@Index(name = "idx_sms_status_next_attempt", columnList = "status, next_attempt_at"),
		@Index(name = "idx_sms_reference_no", columnList = "reference_no") })
public class SmsNotification {

	public static final String PENDING = "PENDING";
	public static final String SENT = "SENT";
	public static final String FAILED = "FAILED";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "mobile_no", length = 20)
	private String mobileNo;

	/** message text; passwords and reset links are masked once the SMS is finished */
	@Column(name = "message", columnDefinition = "TEXT")
	private String message;

	/** e.g. CREDENTIALS, PASSWORD_RESET, GENERAL */
	@Column(name = "sms_type", length = 50)
	private String smsType;

	/** application no / user id the SMS belongs to */
	@Column(name = "reference_no", length = 100)
	private String referenceNo;

	/** PENDING, SENT or FAILED */
	@Column(name = "status", length = 20)
	private String status;

	@Column(name = "attempts")
	private Integer attempts;

	/** last reply of the gateway (or the error) */
	@Column(name = "gateway_response", columnDefinition = "TEXT")
	private String gatewayResponse;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "next_attempt_at")
	private LocalDateTime nextAttemptAt;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}
