package com.moesd.tvet.mis.backend.application.utility;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Calls the G2C SMS gateway: GET {app.sms.url}?mobile=+975XXXXXXXX&message=<text> (the + is URL-encoded as %2B).
 * The gateway can answer HTTP 200 with a failure in the body
 * (e.g. {"status":"403","message":"Access Denied"}), so the body status is checked too.
 */
@Component
public class SmsGatewayClient {

	/** "status":"403" or "status":403 in the gateway reply */
	private static final Pattern BODY_STATUS = Pattern.compile("\"status\"\\s*:\\s*\"?([A-Za-z0-9]+)\"?");

	@Value("${app.sms.url}")
	private String smsUrl;

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();

	/** Result of one gateway call. */
	public record Result(boolean success, String response) {
	}

	/** Never throws; any error is returned as a failed result. */
	public Result push(String mobileNo, String message) {
		try {
			// rows queued before the 975 prefix was added are fixed here too
			String normalized = normalizeMobile(mobileNo);
			String mobile = normalized != null ? normalized : mobileNo;
			String url = smsUrl
					+ "?mobile=" + URLEncoder.encode(mobile, StandardCharsets.UTF_8)
					+ "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
			HttpRequest request = HttpRequest.newBuilder(URI.create(url))
					.timeout(Duration.ofSeconds(10))
					.GET()
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			String body = response.body() == null ? "" : response.body().trim();
			String reply = "HTTP " + response.statusCode() + (body.isEmpty() ? "" : ": " + body);

			if (response.statusCode() < 200 || response.statusCode() >= 300)
				return new Result(false, reply);
			return new Result(!isFailureBody(body), reply);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return new Result(false, "Interrupted");
		} catch (Exception e) {
			return new Result(false, e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}

	/** A body status of 400+ (or error / failed / denied) means the gateway did not send it. */
	private boolean isFailureBody(String body) {
		Matcher m = BODY_STATUS.matcher(body);
		if (m.find()) {
			String status = m.group(1);
			if (status.matches("\\d+"))
				return Integer.parseInt(status) >= 400;
			String s = status.toLowerCase();
			return s.contains("error") || s.contains("fail") || s.contains("denied");
		}
		String lower = body.toLowerCase();
		return lower.contains("access denied") || lower.contains("error");
	}

	/** Bhutan country code (with +) the gateway expects in front of the 8-digit mobile number. */
	private static final String COUNTRY_CODE = "+975";

	/**
	 * Keeps digits only and returns the number as +975 + 8 digits (e.g. 17123456, +975 17123456 and
	 * 97517123456 all become +97517123456); null when it is not an 8-digit Bhutan number.
	 */
	public static String normalizeMobile(String mobileNo) {
		if (mobileNo == null)
			return null;
		String digits = mobileNo.replaceAll("\\D", "");
		if (digits.length() == 11 && digits.startsWith("975"))
			digits = digits.substring(3);
		return digits.length() == 8 ? COUNTRY_CODE + digits : null;
	}
}
