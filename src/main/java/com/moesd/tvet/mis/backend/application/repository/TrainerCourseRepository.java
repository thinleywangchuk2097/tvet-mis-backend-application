package com.moesd.tvet.mis.backend.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moesd.tvet.mis.backend.application.model.TrainerCourse;

import jakarta.transaction.Transactional;

public interface TrainerCourseRepository extends JpaRepository<TrainerCourse, Long> {
	
	@Modifying
	@Transactional
	@Query("DELETE FROM TrainerCourse tc WHERE tc.trainer.id = :trainerId")
	void deleteByTrainerId(@Param("trainerId") Long trainerId);
}
