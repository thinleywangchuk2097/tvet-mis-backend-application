package com.moesd.tvet.mis.backend.application.service;

import java.util.List;
import com.moesd.tvet.mis.backend.application.dto.Privilegedto;

public interface PrivilegeService {
	
	List<Privilegedto> getPrivileges(String roleId);
	
	List<Privilegedto> getParentPrivileges();
	
	List<Privilegedto> getChildPrivileges(String parentId);
}
