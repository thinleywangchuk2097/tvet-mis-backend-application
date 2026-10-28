package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
