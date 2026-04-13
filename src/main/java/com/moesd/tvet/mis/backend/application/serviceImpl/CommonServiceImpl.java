package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import com.moesd.tvet.mis.backend.application.model.Dzongkhag;
import com.moesd.tvet.mis.backend.application.model.InstituteRegistrationQualityStandard;
import com.moesd.tvet.mis.backend.application.model.Occupation;
import com.moesd.tvet.mis.backend.application.model.Sector;
import com.moesd.tvet.mis.backend.application.model.ServiceMaster;
import com.moesd.tvet.mis.backend.application.repository.DropdownChildRepository;
import com.moesd.tvet.mis.backend.application.repository.DzongkhagRepository;
import com.moesd.tvet.mis.backend.application.repository.OccupationRepository;
import com.moesd.tvet.mis.backend.application.repository.QualityStandardRepository;
import com.moesd.tvet.mis.backend.application.repository.SectorRepository;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import com.moesd.tvet.mis.backend.application.service.CommonService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService {

	private final DzongkhagRepository dzongkhagRepository;
	private final SectorRepository sectorRepository;
	private final OccupationRepository occupationRepository;
	private final DropdownChildRepository dropdownChildRepository;
	private final QualityStandardRepository qualityStandardRepository;
    private final ServiceMasterRepository serviceMasterRepository;
	@Override
	public List<Dzongkhag> getAllDzongkhags() {
		return dzongkhagRepository.findAllOrderByName();
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
	public List<InstituteRegistrationQualityStandard> getAllQualitystandards() {
		return qualityStandardRepository.findAll();
	}

	@Override
	public Optional<ServiceMaster> getServiceName(Integer id) {
		// TODO Auto-generated method stub
		return serviceMasterRepository.findById(id);
	}

	@Override
	public List<Occupation> getOccupationsBySectorId(Integer sectorId) {
		// TODO Auto-generated method stub
		return occupationRepository.findBySectorId(sectorId);
	}

	

}
