package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import com.moesd.tvet.mis.backend.application.model.CMFGMonitoring;
import jakarta.persistence.Tuple;

public interface CMFGMonitoringRepository extends JpaRepository<CMFGMonitoring, Long> {

	List<CMFGMonitoring> findByApplicationNoOrderByIdAsc(String applicationNo);

	String SUMMARY_SELECT = "SELECT m.application_no, MIN(m.created_at) AS created_at, "
			+ "  COUNT(*) AS total_items, "
			+ "  SUM(CASE WHEN m.response_id = 'Y' THEN 1 ELSE 0 END) AS yes_count, "
			+ "  SUM(CASE WHEN m.response_id = 'N' THEN 1 ELSE 0 END) AS no_count, "
			+ "  m.institute_id, ir.proposed_institute_name AS institute_name, ir.registration_no, "
			+ "  w.status_id, st.name AS status_name, w.wf_remarks, w.action_date "
			+ "FROM tbl_cmfg_monitoring_dtl m "
			// compare as numbers: avoids "Illegal mix of collations" between the varchar column and CAST(... AS CHAR)
			+ "  LEFT JOIN tbl_institute_registration_dtls ir ON ir.institute_id = CAST(m.institute_id AS UNSIGNED) "
			// explicit collation: the new CMFG table and the workflow table may differ in collation
			+ "  LEFT JOIN tbl_workflow_dtls w ON w.application_no = m.application_no COLLATE utf8mb4_general_ci "
			+ "  LEFT JOIN tbl_dropdown_child_master st ON st.id = w.status_id ";

	String SUMMARY_GROUP = " GROUP BY m.application_no, m.institute_id, ir.proposed_institute_name, ir.registration_no, "
			+ "  w.status_id, st.name, w.wf_remarks, w.action_date ";

	/** One row per submitted monitoring application of the logged-in institute. */
	@NativeQuery(SUMMARY_SELECT + "WHERE ir.registration_no = ?1" + SUMMARY_GROUP + "ORDER BY created_at DESC")
	List<Tuple> getMonitoringByUserId(String userId);

	/** Summary/header of one monitoring application. */
	@NativeQuery(SUMMARY_SELECT + "WHERE m.application_no = ?1" + SUMMARY_GROUP)
	List<Tuple> getMonitoringSummary(String applicationNo);
}
