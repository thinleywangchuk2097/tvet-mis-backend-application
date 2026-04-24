package com.moesd.tvet.mis.backend.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationQualityStandard;

public interface QualityStandardRepository extends JpaRepository<InstituteRegistrationQualityStandard, Integer> {

	List<InstituteRegistrationQualityStandard> findByServiceId(Integer serviceId);
}
