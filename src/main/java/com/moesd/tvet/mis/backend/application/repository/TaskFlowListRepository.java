package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.TaskFlowList;
import jakarta.persistence.Tuple;

public interface TaskFlowListRepository extends JpaRepository <TaskFlowList,Long>{
	
TaskFlowList findByApplicationNo(String applicationNo);
	
	@Query(value =   "SELECT "
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
			+ "WHERE b.task_status_id = ? "
			+ "  AND b.assigned_role_id = ? "
			+ "  AND b.location_id = ? "
			+ "  AND b.assigned_user_id IS NULL", nativeQuery = true)
	List<Tuple>getGroupTaskListDtl(Integer taskStatusId, Integer currentRoleId,String locationId);
	
	@Query(value =    "SELECT "
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
			+ "  AND b.assigned_role_id = ?", nativeQuery = true)
	List<Tuple>getMyTaskListDtl(String userId, String current_roleId);
	
	/* @Query(value =    "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_task_dtls_audit a "
			+ "WHERE a.application_no = ? "
			+ "  AND a.assigned_user_id IS NULL", nativeQuery = true)
	TaskFlowList getInitialTask(String applicationNo); */
}
