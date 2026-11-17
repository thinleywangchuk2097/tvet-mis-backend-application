package com.moesd.tvet.mis.backend.application.serviceimpl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.moesd.tvet.mis.backend.application.dto.NotificationRequestDTO;
import com.moesd.tvet.mis.backend.application.model.Notification;
import com.moesd.tvet.mis.backend.application.repository.NotificationRepository;
import com.moesd.tvet.mis.backend.application.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

	private static final String MESSAGE = "message";
	private static final String DATA = "data";
	private static final String NOT_FOUND_MESSAGE = "Notification not found";

	private final NotificationRepository notificationRepository;

	@Override
	public List<Notification> getAllNotifications() {
		return notificationRepository.findAllByOrderByCreatedAtDesc();
	}

	@Override
	public ResponseEntity<?> createNotification(NotificationRequestDTO request) {
		String error = validate(request);
		if (error != null)
			return badRequest(error);
		String title = request.getTitle().trim();
		if (notificationRepository.existsByTitle(title))
			return badRequest("A notification with this title already exists");

		Notification saved = notificationRepository.save(Notification.builder()
				.title(title)
				.textMessage(request.getTextMessage().trim())
				.createdBy(request.getCreatedBy())
				.createdAt(new Date())
				.build());
		log.info("Notification created: id={}", saved.getId());
		return ResponseEntity.ok(Map.of(MESSAGE, "Notification created successfully", DATA, saved));
	}

	@Override
	public ResponseEntity<?> updateNotification(NotificationRequestDTO request) {
		if (request == null || request.getId() == null)
			return badRequest("Notification id is required");
		String error = validate(request);
		if (error != null)
			return badRequest(error);

		Notification notification = notificationRepository.findById(request.getId()).orElse(null);
		if (notification == null)
			return notFound();

		String title = request.getTitle().trim();
		if (notificationRepository.existsByTitleAndIdNot(title, request.getId()))
			return badRequest("A notification with this title already exists");

		notification.setTitle(title);
		notification.setTextMessage(request.getTextMessage().trim());
		notification.setUpdatedBy(request.getUpdatedBy());
		notification.setUpdatedAt(new Date());
		Notification saved = notificationRepository.save(notification);
		return ResponseEntity.ok(Map.of(MESSAGE, "Notification updated successfully", DATA, saved));
	}

	@Override
	public ResponseEntity<?> deleteNotification(Long id) {
		if (id == null || !notificationRepository.existsById(id))
			return notFound();
		notificationRepository.deleteById(id);
		return ResponseEntity.ok(Map.of(MESSAGE, "Notification deleted successfully"));
	}

	@Override
	public ResponseEntity<?> sendNotification(NotificationRequestDTO request) {
		if (request == null || request.getId() == null)
			return badRequest("Notification id is required");
		Integer type = request.getNotificationType();
		// 0 = public, 1 = selective users, 2 = all registered users
		if (type == null || type < 0 || type > 2)
			return badRequest("Choose Public, Selective User or All Registered User");

		Notification notification = notificationRepository.findById(request.getId()).orElse(null);
		if (notification == null)
			return notFound();

		// once sent, the notification keeps its type (it can't be moved to another audience)
		if (notification.getNotificationType() != null && !notification.getNotificationType().equals(type))
			return badRequest("This notification was already sent as "
					+ typeName(notification.getNotificationType()) + " and can't be sent as another type");

		String userIds = null;
		String roleIds = null;
		if (type == 1) {
			userIds = joinList(request.getUserIds());
			roleIds = joinList(request.getRoleIds());
			if (userIds == null && roleIds == null)
				return badRequest("Select at least one user or role");
		}

		// duration: shown until the end of the chosen day
		String dateError = showUntilError(request.getExpiryDate());
		if (dateError != null)
			return badRequest(dateError);
		LocalDate until = parseShowUntil(request.getExpiryDate());

		notification.setNotificationType(type);
		notification.setExpiryDate(until.atTime(LocalTime.MAX.withNano(0)));
		notification.setUserId(userIds);
		notification.setRoleId(roleIds);
		notification.setUpdatedBy(request.getUpdatedBy());
		notification.setUpdatedAt(new Date());
		Notification saved = notificationRepository.save(notification);
		log.info("Notification {} sent: type={} users={} roles={}", saved.getId(), type, userIds, roleIds);
		return ResponseEntity.ok(Map.of(MESSAGE, sentMessage(type), DATA, saved));
	}

	@Override
	public List<Notification> getPublicNotifications() {
		return notificationRepository.findActivePublic();
	}

	@Override
	public List<Notification> getMyNotifications(String userId, Integer id) {
		if ((userId == null || userId.isBlank()) && id == null)
			return List.of();
		return notificationRepository.findForUser(userId == null ? "" : userId.trim(), id);
	}

	private static String sentMessage(int type) {
		return switch (type) {
			case 0 -> "Notification published on the public page";
			case 2 -> "Notification sent to all registered users";
			default -> "Notification sent to the selected users";
		};
	}

	// "yyyy-MM-dd..." -> date (only the first 10 characters are used)
	private static LocalDate parseShowUntil(String value) {
		return LocalDate.parse(value.trim().substring(0, 10));
	}

	private static String showUntilError(String value) {
		if (value == null || value.isBlank())
			return "Show Until date is required";
		try {
			if (parseShowUntil(value).isBefore(LocalDate.now()))
				return "Show Until date cannot be in the past";
		} catch (DateTimeParseException | IndexOutOfBoundsException e) {
			return "Invalid Show Until date";
		}
		return null;
	}

	private String typeName(Integer type) {
		if (type == null)
			return "Not sent";
		return switch (type) {
			case 0 -> "Public";
			case 1 -> "Selective User";
			case 2 -> "All Registered User";
			default -> "type " + type;
		};
	}

	// ["a", " b ", null] -> "a,b"; empty -> null
	private String joinList(List<?> values) {
		if (values == null)
			return null;
		String joined = values.stream().filter(Objects::nonNull).map(v -> String.valueOf(v).trim())
				.filter(v -> !v.isEmpty()).distinct().collect(Collectors.joining(","));
		return joined.isEmpty() ? null : joined;
	}

	private String validate(NotificationRequestDTO request) {
		if (request == null || request.getTitle() == null || request.getTitle().isBlank())
			return "Title is required";
		if (request.getTextMessage() == null || request.getTextMessage().isBlank())
			return "Message is required";
		return null;
	}

	private ResponseEntity<?> badRequest(String message) {
		return ResponseEntity.badRequest().body(Map.of(MESSAGE, message));
	}

	private ResponseEntity<?> notFound() {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(MESSAGE, NOT_FOUND_MESSAGE));
	}
}
