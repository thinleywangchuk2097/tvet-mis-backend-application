package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.moesd.tvet.mis.backend.application.dto.PublicNoticedto;
import com.moesd.tvet.mis.backend.application.model.Notification;
import com.moesd.tvet.mis.backend.application.repository.PublicNoticeRepository;
import com.moesd.tvet.mis.backend.application.service.PublicNoticeService;
import lombok.RequiredArgsConstructor;

/**
 * Public notices = tbl_notification_dtls rows with notification_type 0.
 * Created already "published": they show on the public page until the end of showUntil.
 */
@Service
@RequiredArgsConstructor
public class PublicNoticeServiceImpl implements PublicNoticeService {

	private static final int PUBLIC = 0;
	private static final int MAX_TITLE = 200;
	private static final int MAX_MESSAGE = 1000;

	private final PublicNoticeRepository repository;

	@Override
	public List<Map<String, Object>> getNotices() {
		return repository.findByNotificationTypeOrderByCreatedAtDesc(PUBLIC).stream().map(this::toMap).toList();
	}

	@Override
	@Transactional
	public Map<String, Object> create(PublicNoticedto request, Integer userId) {
		validate(request);
		String title = request.getTitle().trim();
		if (repository.existsByTitle(title)) {
			throw bad("A notice with this title already exists");
		}
		Notification n = Notification.builder()
				.title(title)
				.textMessage(request.getTextMessage().trim())
				.notificationType(PUBLIC)
				.expiryDate(endOfDay(request.getShowUntil()))
				.createdBy(userId)
				.createdAt(new Date())
				.build();
		return toMap(repository.save(n));
	}

	@Override
	@Transactional
	public Map<String, Object> update(Long id, PublicNoticedto request, Integer userId) {
		Notification n = find(id);
		validate(request);
		String title = request.getTitle().trim();
		if (repository.existsByTitleAndIdNot(title, id)) {
			throw bad("A notice with this title already exists");
		}
		n.setTitle(title);
		n.setTextMessage(request.getTextMessage().trim());
		n.setExpiryDate(endOfDay(request.getShowUntil()));
		n.setUpdatedBy(userId);
		n.setUpdatedAt(new Date());
		return toMap(repository.save(n));
	}

	@Override
	@Transactional
	public void delete(Long id) {
		repository.delete(find(id));
	}

	// ── helpers ─────────────────────────────────────────────────────────
	private Notification find(Long id) {
		Notification n = repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notice not found"));
		// only public notices can be managed here, never user / role notifications
		if (n.getNotificationType() == null || n.getNotificationType() != PUBLIC) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notice not found");
		}
		return n;
	}

	private void validate(PublicNoticedto r) {
		if (r == null) {
			throw bad("Request body is required");
		}
		if (r.getTitle() == null || r.getTitle().isBlank()) {
			throw bad("Title is required");
		}
		if (r.getTitle().trim().length() > MAX_TITLE) {
			throw bad("Title must be " + MAX_TITLE + " characters or fewer");
		}
		if (r.getTextMessage() == null || r.getTextMessage().isBlank()) {
			throw bad("Message is required");
		}
		if (r.getTextMessage().trim().length() > MAX_MESSAGE) {
			throw bad("Message must be " + MAX_MESSAGE + " characters or fewer");
		}
		LocalDate until = parse(r.getShowUntil());
		if (until.isBefore(LocalDate.now())) {
			throw bad("Show until date cannot be in the past");
		}
	}

	private static LocalDate parse(String value) {
		if (value == null || value.isBlank()) {
			throw bad("Show until date is required");
		}
		try {
			return LocalDate.parse(value.trim().substring(0, Math.min(10, value.trim().length())));
		} catch (DateTimeParseException e) {
			throw bad("Show until date must be yyyy-MM-dd");
		}
	}

	private static LocalDateTime endOfDay(String value) {
		return parse(value).atTime(LocalTime.MAX.withNano(0));
	}

	private static ResponseStatusException bad(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}

	private Map<String, Object> toMap(Notification n) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("id", n.getId());
		m.put("title", n.getTitle());
		m.put("textMessage", n.getTextMessage());
		m.put("showUntil", n.getExpiryDate() == null ? null : n.getExpiryDate().toLocalDate().toString());
		m.put("active", n.getExpiryDate() == null || !n.getExpiryDate().isBefore(LocalDateTime.now()));
		m.put("createdAt", n.getCreatedAt());
		m.put("updatedAt", n.getUpdatedAt());
		return m;
	}
}
