package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.model.Dzongkhag;
import com.moesd.tvet.mis.backend.application.model.Gewog;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationQualityStandard;
import com.moesd.tvet.mis.backend.application.model.Occupation;
import com.moesd.tvet.mis.backend.application.model.Sector;
import com.moesd.tvet.mis.backend.application.model.ServiceMaster;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationDetails;

public interface CommonService {

	List<Dzongkhag> getAllDzongkhags();
	
	List<Gewog> getGewogByDzongkhagId(Integer dzongkhagId);

	List<Occupation> getAllOccupations();
	
	List<ServiceMaster> getServiceNameCourseAnnouncement();
	
	List<InstituteRegistrationQualityStandard> getAllQualitystandards(Integer serviceId);

	List<Sector> getAllSectors();
	
	List<Map<String, Object>> getByParentId(Integer parentId);
	
	Optional<ServiceMaster> getServiceName(Integer id);
	
	List<Occupation> getOccupationsBySectorId(Integer sectorId);
	
	List<ObjectNode> getAllCourseAnnouncement();
	
	List<ObjectNode> getCourseAnnouncementByApplicationNo(String application_no);
	
	
}
