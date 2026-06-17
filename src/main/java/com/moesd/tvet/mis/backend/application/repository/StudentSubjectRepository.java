package com.moesd.tvet.mis.backend.application.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moesd.tvet.mis.backend.application.model.StudentSubject;

import jakarta.transaction.Transactional;

public interface StudentSubjectRepository extends JpaRepository<StudentSubject, Long> {
	@Modifying
	@Transactional
	@Query("DELETE FROM StudentSubject ss WHERE ss.student.id = :studentId")
	void deleteByStudentId(@Param("studentId") Long studentId);
}
