package com.moesd.tvet.mis.backend.application.repository;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.moesd.tvet.mis.backend.application.model.RecMemberTaskAssignment;


public interface RecMemberTaskAssignmentRepository extends JpaRepository<RecMemberTaskAssignment, Long> {

	@Query(value = "SELECT COUNT(a.id) "
	        + "FROM tbl_rec_member_task_assignment a "
	        + "WHERE a.remarks IS NULL "
	        + "AND a.application_no = ?1", nativeQuery = true)
	Long getRECMemberCount(String applicationNo);

	@Query(value = "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_rec_member_task_assignment a "
			+ "WHERE a.user_id = ? "
			+ "  AND a.application_no = ?", nativeQuery = true)
	Optional<RecMemberTaskAssignment> findRecMemberUser(String userId, String applicationNo);
}
