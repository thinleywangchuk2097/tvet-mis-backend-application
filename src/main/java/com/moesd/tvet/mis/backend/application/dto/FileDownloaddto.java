package com.moesd.tvet.mis.backend.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
@Getter
public class FileDownloaddto {
	private byte[] fileContent;
    private String contentType;
    private boolean inline;
    private String fileName;
}
