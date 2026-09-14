package com.moesd.tvet.mis.backend.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import com.moesd.tvet.mis.backend.application.model.TaskFlowList;
import com.moesd.tvet.mis.backend.application.model.TaskFlowListAudit;

public interface TaskFlowListAuditRepository extends JpaRepository<TaskFlowListAudit,Long>{
	@NativeQuery("SELECT a.* "
			+ "FROM tbl_task_dtls_audit a "
			+ "WHERE a.application_no = ? "
			+ "  AND a.assigned_user_id IS NULL "
			+ "ORDER BY a.action_date DESC "
			+ "LIMIT 1")
	TaskFlowList getInitialTask(String applicationNo);
}
