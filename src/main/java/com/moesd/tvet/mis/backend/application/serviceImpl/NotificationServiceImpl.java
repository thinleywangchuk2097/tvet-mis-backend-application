package com.moesd.tvet.mis.backend.application.serviceImpl;

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
		return ResponseEntity.ok(Map.of("message", "Notification created successfully", "data", saved));
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
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Notification not found"));

		String title = request.getTitle().trim();
		if (notificationRepository.existsByTitleAndIdNot(title, request.getId()))
			return badRequest("A notification with this title already exists");

		notification.setTitle(title);
		notification.setTextMessage(request.getTextMessage().trim());
		notification.setUpdatedBy(request.getUpdatedBy());
		notification.setUpdatedAt(new Date());
		Notification saved = notificationRepository.save(notification);
		return ResponseEntity.ok(Map.of("message", "Notification updated successfully", "data", saved));
	}

	@Override
	public ResponseEntity<?> deleteNotification(Long id) {
		if (id == null || !notificationRepository.existsById(id))
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Notification not found"));
		notificationRepository.deleteById(id);
		return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
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
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Notification not found"));

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
		if (request.getExpiryDate() == null || request.getExpiryDate().isBlank())
			return badRequest("Show Until date is required");
		LocalDate until;
		try {
			until = LocalDate.parse(request.getExpiryDate().trim().substring(0, 10));
		} catch (DateTimeParseException | IndexOutOfBoundsException e) {
			return badRequest("Invalid Show Until date");
		}
		if (until.isBefore(LocalDate.now()))
			return badRequest("Show Until date cannot be in the past");

		notification.setNotificationType(type);
		notification.setExpiryDate(until.atTime(LocalTime.MAX.withNano(0)));
		notification.setUserId(userIds);
		notification.setRoleId(roleIds);
		notification.setUpdatedBy(request.getUpdatedBy());
		notification.setUpdatedAt(new Date());
		Notification saved = notificationRepository.save(notification);
		log.info("Notification {} sent: type={} users={} roles={}", saved.getId(), type, userIds, roleIds);
		String message = type == 0 ? "Notification published on the public page"
				: type == 2 ? "Notification sent to all registered users"
				: "Notification sent to the selected users";
		return ResponseEntity.ok(Map.of("message", message,
				"data", saved));
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
		return ResponseEntity.badRequest().body(Map.of("message", message));
	}
}
