package com.moesd.tvet.mis.backend.application.utility;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * One place for all SMS + email wording in TVET-MIS.
 * Each method queues both an SMS and an email (via SmsSender / EmailSender), so they are
 * sent and retried by the dispatch jobs. A missing mobile or email only skips that channel.
 *
 * Usage:
 *   notificationService.sendCredentials(mobile, email, name, "Assessor registration", appNo, userId, password);
 *   notificationService.sendStatus(mobile, email, name, "Institute Proposal", appNo, "Rejected", remarks);
 *   notificationService.sendLink(mobile, email, name, subject, purpose, url, note, "TRACER_TRAINEE", uniqueId);
 */
@Component
@RequiredArgsConstructor
public class NotificationService {

	public static final String TYPE_CREDENTIALS = "CREDENTIALS";

	private static final String SMS_PREFIX = "TVET-MIS: ";
	private static final String SIGNATURE = "Thank you.\nTVET-MIS, Ministry of Education and Skills Development";

	private final SmsSender smsSender;
	private final EmailSender emailSender;

	// ==================== CREDENTIALS ====================

	/**
	 * Approved + login details.
	 * @param title e.g. "Assessor registration", "ABC Training Institute registration"
	 */
	public void sendCredentials(String mobile, String email, String name, String title, String applicationNo,
			String userId, String password) {
		String sms = SMS_PREFIX + "Your " + title + appNoPart(applicationNo) + " is approved. User ID: " + userId
				+ ", Password: " + password + ". Please change your password after first login.";
		smsSender.send(mobile, sms, TYPE_CREDENTIALS, userId);

		StringBuilder body = new StringBuilder()
				.append("Dear ").append(greeting(name)).append(",\n\n")
				.append("Your ").append(title).append(" has been approved.\n\n");
		line(body, "Application No", applicationNo);
		line(body, "User ID", userId);
		line(body, "Password", password);
		body.append("\nPlease log in to TVET-MIS and change your password after the first login.\n\n")
				.append(SIGNATURE);
		emailSender.queue(email, capitalize(title) + " Approved - Login Details", body.toString(), false,
				TYPE_CREDENTIALS, userId);
	}

	// ==================== STATUS (approved / rejected / verified ...) ====================

	/** Status change; the type is STATUS_<STATUS>, e.g. STATUS_APPROVED. */
	public void sendStatus(String mobile, String email, String name, String title, String applicationNo,
			String status, String reason) {
		sendStatus(mobile, email, name, title, applicationNo, status, reason, null, null);
	}

	/**
	 * Status change.
	 * @param title    e.g. "proposal for ABC Training Institute", "Accreditor registration"
	 * @param status   e.g. "Approved", "Rejected"
	 * @param reason   shown when given (e.g. the reviewer's remarks on rejection)
	 * @param nextStep optional line for the email (what the applicant should do next)
	 * @param type     notification type; null = STATUS_<STATUS>
	 */
	public void sendStatus(String mobile, String email, String name, String title, String applicationNo,
			String status, String reason, String nextStep, String type) {
		String why = blank(reason) ? null : reason.trim();
		String notificationType = type != null ? type : "STATUS_" + status.toUpperCase().replace(' ', '_');

		String sms = SMS_PREFIX + "Your " + title + appNoPart(applicationNo) + " has been " + status + "."
				+ (why != null ? " Reason: " + why : "");
		smsSender.send(mobile, sms, notificationType, applicationNo);

		StringBuilder body = new StringBuilder()
				.append("Dear ").append(greeting(name)).append(",\n\n")
				.append("Your ").append(title).append(" has been ").append(status.toLowerCase()).append(".\n\n");
		line(body, "Application No", applicationNo);
		line(body, "Status", status);
		if (why != null)
			line(body, "Reason", why);
		if (!blank(nextStep))
			body.append("\n").append(nextStep.trim()).append("\n");
		body.append("\n").append(SIGNATURE);
		emailSender.queue(email,
				capitalize(title) + " " + status + (blank(applicationNo) ? "" : " - " + applicationNo),
				body.toString(), false, notificationType, applicationNo);
	}

	// ==================== LINK (surveys, password reset ...) ====================

	/**
	 * A link the person has to open.
	 * @param subject email subject, e.g. "TVET Graduate Tracer Survey"
	 * @param purpose short sentence before the link, e.g. "please complete the graduate tracer survey"
	 * @param note    optional sentence after the link, e.g. "It expires in 60 minutes."
	 */
	public void sendLink(String mobile, String email, String name, String subject, String purpose, String url,
			String note, String type, String referenceNo) {
		String sms = SMS_PREFIX + "Dear " + greeting(name) + ", " + purpose + ": " + url
				+ (blank(note) ? "" : " " + note.trim());
		smsSender.send(mobile, sms, type, referenceNo);

		String body = "Dear " + greeting(name) + ",\n\n"
				+ capitalize(purpose) + ":\n" + url + "\n\n"
				+ (blank(note) ? "" : note.trim() + "\n\n")
				+ SIGNATURE;
		emailSender.queue(email, subject, body, false, type, referenceNo);
	}

	// ==================== SMS ONLY ====================

	/** Payment advice link by SMS only: "Dear <taxPayerName>, ... <redirectUrl>". */
	public void sendPaymentLink(String mobile, String taxPayerName, String redirectUrl, String paymentAdviceNo) {
		String sms = SMS_PREFIX + "Dear " + greeting(taxPayerName) + ", please make your payment using this link: "
				+ redirectUrl;
		smsSender.send(mobile, sms, "PAYMENT_ADVICE", paymentAdviceNo);
	}

	// ==================== HELPERS ====================

	private static String appNoPart(String applicationNo) {
		return blank(applicationNo) ? "" : " (Application No: " + applicationNo + ")";
	}

	private static String greeting(String name) {
		return blank(name) ? "Applicant" : name.trim();
	}

	/** "Label          : value" with the colons lined up. */
	private static void line(StringBuilder body, String label, String value) {
		body.append(String.format("%-15s: %s\n", label, value == null ? "" : value));
	}

	private static String capitalize(String text) {
		if (blank(text))
			return "";
		String t = text.trim();
		return Character.toUpperCase(t.charAt(0)) + t.substring(1);
	}

	private static boolean blank(String s) {
		return s == null || s.isBlank();
	}
}
