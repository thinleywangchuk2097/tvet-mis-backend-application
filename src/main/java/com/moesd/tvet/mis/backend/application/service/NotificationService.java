package com.moesd.tvet.mis.backend.application.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.moesd.tvet.mis.backend.application.dto.NotificationRequestDTO;
import com.moesd.tvet.mis.backend.application.model.Notification;

public interface NotificationService {

	List<Notification> getAllNotifications();

	ResponseEntity<?> createNotification(NotificationRequestDTO request);

	ResponseEntity<?> updateNotification(NotificationRequestDTO request);

	ResponseEntity<?> deleteNotification(Long id);

	ResponseEntity<?> sendNotification(NotificationRequestDTO request);

	List<Notification> getPublicNotifications();

	List<Notification> getMyNotifications(String userId, Integer id);
}
