package com.moesd.tvet.mis.backend.application.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moesd.tvet.mis.backend.application.model.Notification;
import com.moesd.tvet.mis.backend.application.service.NotificationService;

import lombok.RequiredArgsConstructor;

// Public page notices (notificationType 0); /api/v1/public/** needs no login
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/public/notification")
public class PublicNotificationController {

	private final NotificationService notificationService;

	@GetMapping("/get-public")
	public ResponseEntity<List<Notification>> getPublicNotifications() {
		return ResponseEntity.ok(notificationService.getPublicNotifications());
	}
}
