package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import com.moesd.tvet.mis.backend.application.model.TaskFlowList;
import jakarta.persistence.Tuple;

public interface TaskFlowListRepository extends JpaRepository <TaskFlowList,Long>{
	
TaskFlowList findByApplicationNo(String applicationNo);

	List<TaskFlowList> findAllByApplicationNo(String applicationNo);
	
	@NativeQuery("SELECT "
			+ "  a.application_no, "
			+ "  a.application_name, "
			+ "  DATE(a.action_date) AS action_date, "
			+ "  c.service_name, "
			+ "  d.name AS current_status, "
			+ "  b.save_remarks, "
			+ "  c.route "
			+ "FROM "
			+ "  tbl_workflow_dtls a "
			+ "  LEFT JOIN tbl_task_dtls b "
			+ "    ON a.application_no = b.application_no "
			+ "  LEFT JOIN tbl_service_master c "
			+ "    ON c.id = a.service_id "
			+ "  LEFT JOIN tbl_dropdown_child_master d "
			+ "    ON d.id = a.status_id "
			+ "WHERE b.task_status_id = ? "
			+ "  AND b.assigned_role_id = ? "
			+ "  AND b.location_id = ? "
			+ "  AND b.assigned_user_id IS NULL")
	List<Tuple>getGroupTaskListDtl(Integer taskStatusId, Integer currentRoleId,String locationId);
	
	@NativeQuery("SELECT "
			+ "    a.application_no, "
			+ "    a.application_name, "
			+ "    DATE(a.action_date) AS action_date, "
			+ "    c.service_name, "
			+ "    d.name AS current_status, "
			+ "    c.route "
			+ "FROM tbl_workflow_dtls a "
			+ "LEFT JOIN tbl_task_dtls b "
			+ "    ON a.application_no = b.application_no "
			+ "LEFT JOIN tbl_service_master c "
			+ "    ON c.id = a.service_id "
			+ "LEFT JOIN tbl_dropdown_child_master d "
			+ "    ON d.id = a.status_id "
			+ "WHERE FIND_IN_SET(?, b.assigned_user_id) > 0 "
			+ "  AND b.assigned_role_id = ?")
	List<Tuple>getMyTaskListDtl(String userId, String current_roleId);// current_roleId used for switch role
	
	/* @Query(value =    "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_task_dtls_audit a "
			+ "WHERE a.application_no = ? "
			+ "  AND a.assigned_user_id IS NULL", nativeQuery = true)
	TaskFlowList getInitialTask(String applicationNo); */
	
	@NativeQuery("SELECT "
			+ "  a.application_no, "
			+ "  a.application_name, "
			+ "  DATE(a.action_date) AS action_date, "
			+ "  c.service_name, "
			+ "  d.name AS current_status, "
			+ "  c.route "
			+ "FROM "
			+ "  tbl_workflow_dtls a "
			+ "  LEFT JOIN tbl_task_dtls b "
			+ "    ON a.application_no = b.application_no "
			+ "  LEFT JOIN tbl_service_master c "
			+ "    ON c.id = a.service_id "
			+ "  LEFT JOIN tbl_dropdown_child_master d "
			+ "    ON d.id = a.status_id "
			+ "WHERE b.assigned_user_id = ? "
			+ "  AND b.assigned_role_id = ?"
			+ "  AND b.assigned_role_id = ?")
	List<Tuple>getApplicationStatusDtl(String application_no, String applicant_name, String application_date);
	
	
	// Application history + current step:
	//  tbl_workflow_dtls = current step, tbl_workflow_dtls_audit = earlier steps (copied on each update).
	//  Turnaround = action date -> now, for every row.
	// Only numeric joins here (tbl_role on role_id, tbl_dropdown_child_master on status_id). User names and the current task (tbl_task_dtls) are
	// added in TaskFlowListServiceImpl, so no text column is compared across tables.
	@NativeQuery("SELECT * FROM ( "
			+ "SELECT 'CURRENT' AS record_type, w.id, w.application_no, w.application_name, w.status_id, "
			+ "       s.name AS status_name, w.service_id, w.role_id, r.role_name, w.actor_id, w.wf_remarks, "
			+ "       w.action_date, "
			+ "       TIMESTAMPDIFF(MINUTE, w.action_date, NOW()) AS turnaround_minutes "
			+ "  FROM tbl_workflow_dtls w "
			+ "  LEFT JOIN tbl_role r ON r.id = w.role_id "
			+ "  LEFT JOIN tbl_dropdown_child_master s ON s.id = w.status_id "
			+ " WHERE w.application_no = ?1 "
			+ "UNION ALL "
			+ "SELECT 'AUDIT' AS record_type, b.id, b.application_no, b.application_name, b.status_id, "
			+ "       s.name AS status_name, b.service_id, b.role_id, r.role_name, b.actor_id, b.wf_remarks, "
			+ "       COALESCE(b.created_at, b.action_date) AS action_date, "
			+ "       TIMESTAMPDIFF(MINUTE, COALESCE(b.created_at, b.action_date), NOW()) AS turnaround_minutes "
			+ "  FROM tbl_workflow_dtls_audit b "
			+ "  LEFT JOIN tbl_role r ON r.id = b.role_id "
			+ "  LEFT JOIN tbl_dropdown_child_master s ON s.id = b.status_id "
			+ " WHERE b.application_no = ?1 "
			+ ") h "
			// oldest step first, by action date (the current step is the latest, so it comes last)
			+ "ORDER BY h.action_date, h.id")
	List<Tuple> getApplicationStatusAuditCurrentTaskDtl(String applicationNo);

	// Names for a list of tbl_user.user_id values (current task holders)
	@NativeQuery("SELECT u.user_id AS user_id, "
			+ "       TRIM(CONCAT_WS(' ', u.first_name, NULLIF(u.middle_name, ''), u.last_name)) AS name "
			+ "  FROM tbl_user u WHERE u.user_id IN (?1)")
	List<Tuple> findUserNamesByUserIds(List<String> userIds);

	// REC members who have already completed their part (tbl_rec_member_task_assignment.remarks filled)
	@NativeQuery("SELECT r.user_id FROM tbl_rec_member_task_assignment r "
			+ " WHERE r.application_no = ?1 AND r.remarks IS NOT NULL")
	List<String> findCompletedRecUserIds(String applicationNo);
}
