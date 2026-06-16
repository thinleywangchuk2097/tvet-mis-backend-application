package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.moesd.tvet.mis.backend.application.model.Subject;

import jakarta.persistence.Tuple;

public interface SubjectRepository extends JpaRepository<Subject, Long>{
	
	Optional<Subject> findBySubjectName(String subjectName);
	
	@Query(value =  "SELECT "
			+ "  a.* "
			+ "FROM "
			+ "  tbl_subject_dtls a "
			+ "WHERE a.status_id = 1 "
			+ "  AND a.institute_id = ?", nativeQuery = true)
	List<Tuple> getAllActiveSubjects(Integer institute_id);
	
}
