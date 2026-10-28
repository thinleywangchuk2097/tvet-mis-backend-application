package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.TOTTrainerModuleResult;

public interface TOTTrainerModuleResultRepository extends JpaRepository<TOTTrainerModuleResult, Long> {

	Optional<TOTTrainerModuleResult> findByApplicationNoAndTrainerIdAndModuleId(
			String applicationNo, Integer trainerId, Long moduleId);

	List<TOTTrainerModuleResult> findByApplicationNo(String applicationNo);
}
