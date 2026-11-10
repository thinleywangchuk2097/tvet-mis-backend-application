package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moesd.tvet.mis.backend.application.dto.NotificationRequestDTO;
import com.moesd.tvet.mis.backend.application.model.Notification;
import com.moesd.tvet.mis.backend.application.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/management/notification")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping("/get-all")
	public ResponseEntity<List<Notification>> getAllNotifications() {
		return ResponseEntity.ok(notificationService.getAllNotifications());
	}

	@PostMapping("/create")
	public ResponseEntity<?> createNotification(@RequestBody NotificationRequestDTO request) {
		return notificationService.createNotification(request);
	}

	@PutMapping("/update")
	public ResponseEntity<?> updateNotification(@RequestBody NotificationRequestDTO request) {
		return notificationService.updateNotification(request);
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
		return notificationService.deleteNotification(id);
	}

	// send: notificationType 0 = public, 1 = selective users (userIds / roleIds), 2 = all registered users
	@PutMapping("/send")
	public ResponseEntity<?> sendNotification(@RequestBody NotificationRequestDTO request) {
		return notificationService.sendNotification(request);
	}

	// header bell: notifications sent to this user or to one of the user's roles
	@GetMapping("/get-my-notifications")
	public ResponseEntity<List<Notification>> getMyNotifications(
			@RequestParam(required = false) String userId,
			@RequestParam(required = false) Integer id) {
		return ResponseEntity.ok(notificationService.getMyNotifications(userId, id));
	}
}
