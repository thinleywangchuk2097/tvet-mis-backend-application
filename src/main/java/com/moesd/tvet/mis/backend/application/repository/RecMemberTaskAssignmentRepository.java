package com.moesd.tvet.mis.backend.application.repository;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
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
}
