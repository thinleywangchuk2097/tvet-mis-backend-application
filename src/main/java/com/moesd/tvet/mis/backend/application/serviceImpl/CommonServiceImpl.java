package com.moesd.tvet.mis.backend.application.serviceImpl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.moesd.tvet.mis.backend.application.model.Dzongkhag;
import com.moesd.tvet.mis.backend.application.repository.DzongkhagRepository;
import com.moesd.tvet.mis.backend.application.service.CommonService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommonServiceImpl implements CommonService{

	private final DzongkhagRepository dzongkhagRepository;
	
	@Override
	public List<Dzongkhag> getAllDzongkhags() {
		// TODO Auto-generated method stub
		return dzongkhagRepository.findAllOrderByName();
	}
	
	


}
