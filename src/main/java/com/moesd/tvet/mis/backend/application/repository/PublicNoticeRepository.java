package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.Notification;

/**
 * Public notices (the scrolling notice bar on the public home page) are rows of
 * tbl_notification_dtls with notification_type = 0. Managed by TVET QC.
 */
public interface PublicNoticeRepository extends JpaRepository<Notification, Long> {

	List<Notification> findByNotificationTypeOrderByCreatedAtDesc(Integer notificationType);

	boolean existsByTitle(String title);

	boolean existsByTitleAndIdNot(String title, Long id);
}
