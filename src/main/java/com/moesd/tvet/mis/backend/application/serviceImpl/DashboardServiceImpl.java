package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.moesd.tvet.mis.backend.application.repository.DashboardRepository;
import com.moesd.tvet.mis.backend.application.service.DashboardService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

	private final DashboardRepository dashboardRepository;

	@Override
	public List<Map<String, Object>> getDashboardData(Long userId, Long currentRoleId) {
		return dashboardRepository.getDashboardData(userId, currentRoleId);
	}
}
