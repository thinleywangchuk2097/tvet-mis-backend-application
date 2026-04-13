package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.moesd.tvet.mis.backend.application.model.Dzongkhag;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationQualityStandard;
import com.moesd.tvet.mis.backend.application.model.Occupation;
import com.moesd.tvet.mis.backend.application.model.Sector;
import com.moesd.tvet.mis.backend.application.model.ServiceMaster;

public interface CommonService {

	List<Dzongkhag> getAllDzongkhags();

	List<Occupation> getAllOccupations();
	
	List<InstituteRegistrationQualityStandard> getAllQualitystandards();

	List<Sector> getAllSectors();
	
	List<Map<String, Object>> getByParentId(Integer parentId);
	
	Optional<ServiceMaster> getServiceName(Integer id);
	
	List<Occupation> getOccupationsBySectorId(Integer sectorId);
}
