package com.moesd.tvet.mis.backend.application.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.moesd.tvet.mis.backend.application.model.TracerSurveySendDetails;

public interface TracerSurveySendDetailsRepository extends JpaRepository<TracerSurveySendDetails, Long>{
	Optional<TracerSurveySendDetails> findByUniqueId(String uniqueId);

	// Every survey sent for one tracer (questionApplicationNo; older rows fall back to applicationNo)
	@Query("SELECT s FROM TracerSurveySendDetails s "
			+ "WHERE s.questionApplicationNo = :tracerNo "
			+ "OR ((s.questionApplicationNo IS NULL OR s.questionApplicationNo = '') AND s.applicationNo = :tracerNo) "
			+ "ORDER BY s.createdAt DESC")
	List<TracerSurveySendDetails> findSendsForTracer(@Param("tracerNo") String tracerNo);

	// Atomically claim the link for submission (status_id NULL = not submitted, 55 = submitted).
	// Returns 1 for the first submit, 0 if it was already submitted - the row lock makes a
	// second, simultaneous request wait and then see 55, so only one submit can win.
	@Modifying
	@Query("UPDATE TracerSurveySendDetails s SET s.statusId = 55, s.updatedAt = :now "
			+ "WHERE s.uniqueId = :uniqueId AND s.statusId IS NULL")
	int markSubmitted(@Param("uniqueId") String uniqueId, @Param("now") LocalDateTime now);
}
