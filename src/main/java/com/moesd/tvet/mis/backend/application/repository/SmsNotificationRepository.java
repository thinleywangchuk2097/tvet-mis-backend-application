package com.moesd.tvet.mis.backend.application.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.SmsNotification;

public interface SmsNotificationRepository extends JpaRepository<SmsNotification, Long> {

	/** SMS waiting to be sent whose next attempt is due, oldest first (batch of 20). */
	List<SmsNotification> findTop20ByStatusAndNextAttemptAtLessThanEqualOrderByIdAsc(String status,
			LocalDateTime now);
}
