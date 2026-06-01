package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.TracerSurveyResponseDetails;

public interface TracerSurveyResponseDetailsRepository extends JpaRepository<TracerSurveyResponseDetails, Long>{
	 List<TracerSurveyResponseDetails> findByApplicationNo(String applicationNo);
}
