package com.moesd.tvet.mis.backend.application.utility;

/** Hides secrets in stored SMS / email text once the message is finished (sent or failed). */
public final class NotificationMasker {

	private NotificationMasker() {
	}

	public static String mask(String text) {
		if (text == null)
			return null;
		return text
				// "Password: abc123#"  -> "Password: ****"
				.replaceAll("(?i)(password\\s*[:=]\\s*)[^\\s,<]+", "$1****")
				// reset links "...?token=eyJ..." -> "...?token=****"
				.replaceAll("(?i)(token=)[^\\s&\"'<]+", "$1****");
	}
}
