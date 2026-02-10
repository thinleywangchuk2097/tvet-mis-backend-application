package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.moesd.tvet.mis.backend.application.dto.Privilegedto;
import com.moesd.tvet.mis.backend.application.repository.PrivilegeRepository;
import com.moesd.tvet.mis.backend.application.service.PrivilegeService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrivilegeServiceImpl implements PrivilegeService{
	
private final PrivilegeRepository privilegeRepository;
	
	@Override
	public List<Privilegedto> getPrivileges(String roleId) {
		return (privilegeRepository.getPrivileges(roleId));
	}

	@Override
	public List<Privilegedto> getParentPrivileges() {
		return (privilegeRepository.getParentPrivileges());
	}

	@Override
	public List<Privilegedto> getChildPrivileges(String parentId) {
		return (privilegeRepository.getChildPrivileges(parentId));
	}

}
