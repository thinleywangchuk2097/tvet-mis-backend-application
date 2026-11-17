package com.moesd.tvet.mis.backend.application.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.EmailNotification;

public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {

	/** Emails waiting to be sent whose next attempt is due, oldest first (batch of 20). */
	List<EmailNotification> findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(String status,
			LocalDateTime now);
}
