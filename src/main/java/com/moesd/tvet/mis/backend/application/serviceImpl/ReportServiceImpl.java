package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.moesd.tvet.mis.backend.application.repository.ReportRepository;
import com.moesd.tvet.mis.backend.application.service.ReportService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

	private final ReportRepository reportRepository;

	@Override
	public List<Map<String, Object>> getcourseData() {
		return reportRepository.getcourseData();
	}

	@Override
	public List<Map<String, Object>> courseServiceType() {
		
		return reportRepository.courseServiceType();
	}

	@Override
	public List<Map<String, Object>> getAllInstitutes() {
		
		return reportRepository.getAllInstitutes();
	}

	@Override
	public List<Map<String, Object>> getInstitutesProposalType() {
		return reportRepository.getInstitutesProposalType();
	}
	
	@Override
	public List<Map<String, Object>> getInstituteRegistrationType() {
		return reportRepository.getInstituteRegistrationType();
	}

	@Override
	public List<Map<String, Object>> getInstitutesProposalDetails() {
		return reportRepository.getInstitutesProposalDetails();
	}

	@Override
	public List<Map<String, Object>> getInstitutesRegistrationDetails() {
		return reportRepository.getInstitutesRegistrationDetails();
	}

	@Override
	public List<Map<String, Object>> getInstitutesTraineesDetails() {
		return reportRepository.getInstitutesTraineesDetails();
	}
}
