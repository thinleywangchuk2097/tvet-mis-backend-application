package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.TOTTrainerContentMarks;

public interface TOTTrainerContentMarksRepository extends JpaRepository<TOTTrainerContentMarks, Long> {

	Optional<TOTTrainerContentMarks> findByApplicationNoAndTrainerIdAndCourseContentId(
			String applicationNo, Integer trainerId, Long courseContentId);

	List<TOTTrainerContentMarks> findByApplicationNo(String applicationNo);
}
