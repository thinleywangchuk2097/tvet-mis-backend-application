package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moesd.tvet.mis.backend.application.model.Dzongkhag;
import com.moesd.tvet.mis.backend.application.model.Gewog;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationQualityStandard;
import com.moesd.tvet.mis.backend.application.model.Occupation;
import com.moesd.tvet.mis.backend.application.model.Sector;
import com.moesd.tvet.mis.backend.application.model.ServiceMaster;
import com.moesd.tvet.mis.backend.application.repository.CourseEnrollmentAppRepository;
import com.moesd.tvet.mis.backend.application.repository.DropdownChildRepository;
import com.moesd.tvet.mis.backend.application.repository.DzongkhagRepository;
import com.moesd.tvet.mis.backend.application.repository.GewogRepository;
import com.moesd.tvet.mis.backend.application.repository.OccupationRepository;
import com.moesd.tvet.mis.backend.application.repository.QualityStandardRepository;
import com.moesd.tvet.mis.backend.application.repository.SectorRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.CommonService;
import com.moesd.tvet.mis.backend.application.utility.ObjectToJson;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

	private final DzongkhagRepository dzongkhagRepository;
	private final GewogRepository gewogRepository;
	private final SectorRepository sectorRepository;
	private final OccupationRepository occupationRepository;
	private final DropdownChildRepository dropdownChildRepository;
	private final QualityStandardRepository qualityStandardRepository;
    private final ServiceMasterRepository serviceMasterRepository;
    private final CourseEnrollmentAppRepository courseEnrollmentAppRepository;
	private final ObjectToJson objectTojson;
	
	

	@Override
	public List<Dzongkhag> getAllDzongkhags() {
		return dzongkhagRepository.findAllOrderByName();
	}
	
	@Override
	public List<Gewog> getGewogByDzongkhagId(Integer dzongkhagId) {
		return gewogRepository.getGewogByDzongkhagId(dzongkhagId);
	}
	
	@Override
	public List<Occupation> getAllOccupations() {
		return occupationRepository.findAll();
	}

	@Override
	public List<Sector> getAllSectors() {
		return sectorRepository.findAll();
	}

	@Override
	public List<Map<String, Object>> getByParentId(Integer parentId) {
		List<Map<String, Object>> children = dropdownChildRepository.findChildByParentId(parentId);
		return children;
	}

	@Override
	public List<InstituteRegistrationQualityStandard> getAllQualitystandards(Integer serviceId) {
		return qualityStandardRepository.findByServiceId(serviceId);
	}

	@Override
	public Optional<ServiceMaster> getServiceName(Integer id) {
		return serviceMasterRepository.findById(id);
	}

	@Override
	public List<Occupation> getOccupationsBySectorId(Integer sectorId) {
		return occupationRepository.findBySectorId(sectorId);
	}


	@Override
	public List<ObjectNode> getAllCourseAnnouncement() {
		List<Tuple> resultList = courseEnrollmentAppRepository.getAllCourseAnnouncement();
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ObjectNode> getCourseAnnouncementByApplicationNo(String application_no) {
		List<Tuple> resultList = courseEnrollmentAppRepository.getCourseAnnouncementByApplicationNo(application_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	@Override
	public List<ServiceMaster> getServiceNameCourseAnnouncement() {
		return serviceMasterRepository.getServiceNameCourseAnnouncement();
	}

	@Override
	public List<ObjectNode> getReAssessmentAnnouncementByApplicationNo(String application_no) {
		List<Tuple> resultList = courseEnrollmentAppRepository.getReAssessmentAnnouncementByApplicationNo(application_no);
		List<ObjectNode> DtlsJson = objectTojson._toJson(resultList);
		return DtlsJson;
	}

	

	


}
