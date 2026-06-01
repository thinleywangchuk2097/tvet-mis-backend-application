package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

import com.moesd.tvet.mis.backend.application.repository.PublicPageRepository;
import com.moesd.tvet.mis.backend.application.service.PublicPageService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicPageServiceImpl implements PublicPageService {
	
	private final PublicPageRepository publicPageRepository;
	
	@Override
	public List<Map<String, Object>> trackApplicationStatus(String applicationNo) {
		return publicPageRepository.trackApplicationStatus(applicationNo);
	}

	@Override
	public List<Map<String, Object>> getOngoingCourses() {
		return publicPageRepository.getOngoingCourses();
	}

	@Override
	public List<Map<String, Object>> getAllInstitutes() {
		return publicPageRepository.getAllInstitutes();
	}

	@Override
	public List<Map<String, Object>> getCourseBySector() {
		return publicPageRepository.getCourseBySector();
	}

	@Override
	public List<Map<String, Object>> getCourseAnnounceNotifications() {
		return publicPageRepository.getCourseAnnounceNotifications();
	}

}
