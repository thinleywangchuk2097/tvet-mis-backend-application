package com.moesd.tvet.mis.backend.application.utility;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Sends SMS through the G2C SMS gateway:
 * GET {app.sms.url}?mobile=<mobile>&message=<text>
 * Failures are logged and reported as false; they never throw, so an SMS problem
 * cannot break the action that triggered it.
 */
@Slf4j
@Component
public class SmsSender {

	@Value("${app.sms.url}")
	private String smsUrl;

	@Value("${app.sms.enabled:true}")
	private boolean enabled;

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();

	/** @return true when the gateway accepted the request (HTTP 2xx) */
	public boolean send(String mobileNo, String message) {
		String mobile = normalize(mobileNo);
		if (!enabled) {
			log.info("SMS disabled; not sending to {}", mobile);
			return false;
		}
		if (mobile == null) {
			log.warn("SMS not sent: invalid mobile number '{}'", mobileNo);
			return false;
		}
		if (message == null || message.isBlank()) {
			log.warn("SMS not sent to {}: empty message", mobile);
			return false;
		}
		try {
			String url = smsUrl
					+ "?mobile=" + URLEncoder.encode(mobile, StandardCharsets.UTF_8)
					+ "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
			HttpRequest request = HttpRequest.newBuilder(URI.create(url))
					.timeout(Duration.ofSeconds(10))
					.GET()
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			boolean ok = response.statusCode() >= 200 && response.statusCode() < 300;
			if (ok) {
				log.info("SMS sent to {} (gateway response: {})", mobile, response.body());
			} else {
				log.warn("SMS gateway returned {} for {}: {}", response.statusCode(), mobile, response.body());
			}
			return ok;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.error("SMS to {} interrupted", mobile, e);
			return false;
		} catch (Exception e) {
			log.error("Failed to send SMS to {}", mobile, e);
			return false;
		}
	}

	/** Keeps digits only; Bhutan numbers are 8 digits (a leading 975 country code is dropped). */
	private String normalize(String mobileNo) {
		if (mobileNo == null)
			return null;
		String digits = mobileNo.replaceAll("\\D", "");
		if (digits.length() == 11 && digits.startsWith("975"))
			digits = digits.substring(3);
		return digits.length() == 8 ? digits : null;
	}
}
