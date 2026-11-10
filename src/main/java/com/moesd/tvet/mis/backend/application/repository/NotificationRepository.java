package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import com.moesd.tvet.mis.backend.application.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	// newest first (notification page and the header bell)
	List<Notification> findAllByOrderByCreatedAtDesc();

	boolean existsByTitle(String title);

	boolean existsByTitleAndIdNot(String title, Long id);

	// public page: public notifications (type 0) that haven't expired
	@NativeQuery("SELECT n.* FROM tbl_notification_dtls n "
			+ "WHERE n.notification_type = 0 "
			+ "  AND (n.expiry_date IS NULL OR n.expiry_date >= NOW()) "
			+ "ORDER BY COALESCE(n.updated_at, n.created_at) DESC")
	List<Notification> findActivePublic();

	// header bell (only ones not expired): all-registered-user notifications (type 2), plus selective ones (type 1)
	// sent to this user (user_id list) or to one of this user's roles (tbl_user_role on the numeric user id)
	@NativeQuery("SELECT n.* FROM tbl_notification_dtls n "
			+ "WHERE (n.expiry_date IS NULL OR n.expiry_date >= NOW()) "
			+ "  AND (n.notification_type = 2 "
			+ "       OR (n.notification_type = 1 "
			+ "           AND (FIND_IN_SET(?1, n.user_id) > 0 "
			+ "                OR EXISTS (SELECT 1 FROM tbl_user_role ur "
			+ "                           WHERE ur.user_id = ?2 AND FIND_IN_SET(ur.role_id, n.role_id) > 0)))) "
			+ "ORDER BY COALESCE(n.updated_at, n.created_at) DESC")
	List<Notification> findForUser(String userId, Integer id);
}
