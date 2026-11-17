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
 * Every email the system sends: queued as PENDING, sent by EmailDispatchJob,
 * retried on failure, then marked SENT or FAILED.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_email_notification_dtls", indexes = {
		@Index(name = "idx_email_status_next_attempt", columnList = "status, next_attempt_at"),
		@Index(name = "idx_email_reference_no", columnList = "reference_no") })
public class EmailNotification {

	public static final String PENDING = "PENDING";
	public static final String SENT = "SENT";
	public static final String FAILED = "FAILED";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "email_to", length = 255)
	private String emailTo;

	@Column(name = "subject", length = 500)
	private String subject;

	/** email body; passwords and reset links are masked once the email is finished */
	@Column(name = "body", columnDefinition = "LONGTEXT")
	private String body;

	/** true = HTML body, false = plain text */
	@Column(name = "is_html")
	private Boolean html;

	/** e.g. CREDENTIALS, PASSWORD_RESET, GENERAL */
	@Column(name = "email_type", length = 50)
	private String emailType;

	/** application no / user id the email belongs to */
	@Column(name = "reference_no", length = 100)
	private String referenceNo;

	/** PENDING, SENT or FAILED */
	@Column(name = "status", length = 20)
	private String status;

	@Column(name = "attempts")
	private Integer attempts;

	/** last error (null when sent) */
	@Column(name = "last_error", columnDefinition = "TEXT")
	private String lastError;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "next_attempt_at")
	private LocalDateTime nextAttemptAt;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}
