package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Documentdto {
	private byte[] content;
    private String name;
    private String originalFilename;
    private String contentType;
    private String path;
}
