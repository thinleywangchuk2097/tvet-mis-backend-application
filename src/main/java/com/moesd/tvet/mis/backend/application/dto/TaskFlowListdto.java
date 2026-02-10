package com.moesd.tvet.mis.backend.application.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskFlowListdto {
	private String applicationNo;
	private String assignedUserId;
	private Integer taskStatusId;
	private Date actionDate;
	private String assignedRoleId;
	private Integer locationId;
	private Integer statusId;
	private Integer serviceId;
	private String actorId;
    private Integer roleId;
    private String remarks;

}
