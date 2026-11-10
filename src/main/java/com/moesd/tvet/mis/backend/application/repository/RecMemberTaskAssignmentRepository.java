package com.moesd.tvet.mis.backend.application.repository;


import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import com.moesd.tvet.mis.backend.application.model.RecMemberTaskAssignment;


public interface RecMemberTaskAssignmentRepository extends JpaRepository<RecMemberTaskAssignment, Long> {

	@NativeQuery("SELECT COUNT(a.id) "
	        + "FROM tbl_rec_member_task_assignment a "
	        + "WHERE a.remarks IS NULL "
	        + "AND a.application_no = ?1")
	Long getRECMemberCount(String applicationNo);

	@NativeQuery("SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_rec_member_task_assignment a "
			+ "WHERE a.user_id = ? "
			+ "  AND a.application_no = ?")
	Optional<RecMemberTaskAssignment> findRecMemberUser(String userId, String applicationNo);

	// ---- REC members of one application/service (used by the Self Assessment Report) ----

	@NativeQuery("SELECT DISTINCT a.user_id "
			+ "FROM tbl_rec_member_task_assignment a "
			+ "WHERE a.application_no = ?1 AND a.service_id = ?2 "
			+ "AND a.user_id IS NOT NULL AND a.user_id <> ''")
	List<String> findUserIdsByApplicationNoAndServiceId(String applicationNo, Integer serviceId);

	@NativeQuery("SELECT COUNT(a.id) "
			+ "FROM tbl_rec_member_task_assignment a "
			+ "WHERE a.application_no = ?1 AND a.service_id = ?2 AND a.remarks IS NOT NULL")
	Long countCompletedByApplicationNoAndServiceId(String applicationNo, Integer serviceId);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM tbl_rec_member_task_assignment WHERE application_no = ?1 AND service_id = ?2",
			nativeQuery = true)
	void deleteByApplicationNoAndServiceId(String applicationNo, Integer serviceId);
}
