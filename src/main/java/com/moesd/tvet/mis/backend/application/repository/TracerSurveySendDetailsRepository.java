package com.moesd.tvet.mis.backend.application.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.TracerSurveySendDetails;

public interface TracerSurveySendDetailsRepository extends JpaRepository<TracerSurveySendDetails, Long>{
	Optional<TracerSurveySendDetails> findByUniqueId(String uniqueId);
}
