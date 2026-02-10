package com.moesd.tvet.mis.backend.application.utility;

import org.springframework.stereotype.Service;

import com.moesd.tvet.mis.backend.application.model.ServiceMaster;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenerateApplicationNumber {
	private final ServiceMasterRepository serviceMasterRepository;

	public String generateApplicationNumber(Integer serviceId) {
		ServiceMaster serviceMaster = serviceMasterRepository.findById(serviceId)
				.orElseThrow(() -> new RuntimeException("Service master not found"));
		String formattedLastApplicationNo = String.format("%07d", serviceMaster.getLastApplicationNo());
		serviceMaster.setLastApplicationNo(serviceMaster.getLastApplicationNo() + 1);
		serviceMasterRepository.save(serviceMaster);
		return serviceId + formattedLastApplicationNo;
	}
}
