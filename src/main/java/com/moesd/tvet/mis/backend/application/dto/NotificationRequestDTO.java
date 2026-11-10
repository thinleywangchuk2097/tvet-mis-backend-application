package com.moesd.tvet.mis.backend.application.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
	private Long id;
	private String title;
	private String textMessage;
	private Integer createdBy;
	private Integer updatedBy;

	// send: 0 = public, 1 = selective users (by user ids and/or role ids), 2 = all registered users
	private Integer notificationType;
	private List<String> userIds;
	private List<Integer> roleIds;

	// last day the notification is shown, "yyyy-MM-dd" (it disappears after that day)
	private String expiryDate;
}
