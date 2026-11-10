package com.moesd.tvet.mis.backend.application.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tbl_notification_dtls")
public class Notification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "title", nullable = false, unique = true)
	private String title;
	
	// TEXT so bulk messages fit (a unique index can't be put on a TEXT column)
	@Column(name = "text_message", nullable = false, columnDefinition = "TEXT")
	private String textMessage;
	
	// NULL = not sent yet, 0 = public, 1 = selective users (user/role lists), 2 = all registered users
	private Integer notificationType;
	
	// comma separated tbl_user.user_id values (registered users)
	@Column(name="user_id",columnDefinition = "TEXT")
	private String userId;
	
	// comma separated tbl_role.id values (registered users); String because it holds a list
	@Column(name="role_id",columnDefinition = "TEXT")
	private String roleId;
	
	// shown until this moment (end of the chosen day); NULL = no end date
	@Column(name = "expiry_date")
	private LocalDateTime expiryDate;
	
	private Integer createdBy;

	private Date createdAt;

	private Integer updatedBy;

	private Date updatedAt;
}
