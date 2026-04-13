package com.moesd.tvet.mis.backend.application.utility;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.moesd.tvet.mis.backend.application.model.ServiceMaster;
import com.moesd.tvet.mis.backend.application.repository.ServiceMasterRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenerateLicenseNumber {

	private final ServiceMasterRepository serviceMasterRepository;

	public String generateLicenseNumber(Integer serviceId) {
		ServiceMaster serviceMaster = serviceMasterRepository.findById(serviceId)
				.orElseThrow(() -> new RuntimeException("Service master not found"));
		//Get current year and month (6 digits: YYYYMM)
		LocalDate currentDate = LocalDate.now();
		String yearMonth = currentDate.format(DateTimeFormatter.ofPattern("yyyyMM"));
		//Format the license number with year-month (6 digits) and sequence (4 digits) total 10 digits
		//String formattedLicenseNo = String.format("%s%04d", yearMonth, serviceMaster.getLicenseLastSequence());
		String formattedLicenseNo = String.format("%s%d%04d", yearMonth, serviceId, serviceMaster.getLicenseLastSequence());
		//Increment and save the sequence
		serviceMaster.setLicenseLastSequence(serviceMaster.getLicenseLastSequence() + 1);
		serviceMasterRepository.save(serviceMaster);
		System.out.println("formattedLicenseNo" + formattedLicenseNo);
		return formattedLicenseNo;
	}
}
