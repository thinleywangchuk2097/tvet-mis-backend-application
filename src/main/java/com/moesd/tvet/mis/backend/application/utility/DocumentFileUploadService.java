package com.moesd.tvet.mis.backend.application.utility;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import com.moesd.tvet.mis.backend.application.dto.Documentdto;
import com.moesd.tvet.mis.backend.application.model.DocumentFile;
import com.moesd.tvet.mis.backend.application.repository.DocumentRepository;
import jakarta.annotation.Nullable;

@Service
public class DocumentFileUploadService {
	@Value("${file.store.path}")
    private String fileStorePath;
	
	 @Autowired
	 private DocumentRepository documentRepository;
	 
	 public void saveDocument(Documentdto[] documentdto, String applicationNo, String directoryName, Integer serviceId,Integer userId, @Nullable String attachmentType) {
	        for (Documentdto file : documentdto) {
	            String name = file.getName();
	            byte[] content = file.getContent();
	            String fileType = file.getContentType();
	            
	            String randomUUID = UUID.randomUUID().toString();
	            String uuid = randomUUID.replaceAll("-", "");
	            
	            Calendar calendar = Calendar.getInstance();
	            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM");
	            String urlAppender = "/" + directoryName + "/" + calendar.get(Calendar.YEAR) + "/"
	                    + dateFormat.format(calendar.getTime()) + "/" + calendar.get(Calendar.DATE) + "/";
	            String filePath = fileStorePath + urlAppender + uuid + "_" + name;
	            File fileloc = new File(fileStorePath + urlAppender);
	            
	            if (!fileloc.exists()) {
	                boolean dirsCreated = fileloc.mkdirs();
	                if (!dirsCreated) {
	                    // Handle the failure accordingly, e.g., throw an exception or return.
	                    continue; // Skip to the next iteration
	                }
	            }

	            try {
	                FileCopyUtils.copy(content, new File(filePath));
	            } catch (IOException e) {
	                e.printStackTrace(); // Print stack trace for debugging
	                // Handle the exception, e.g., throw an exception or log the error.
	                continue; // Skip to the next iteration
	            }
	            
	            DocumentFile documentSave = DocumentFile.builder()
	                    .applicationNo(applicationNo)
	                    .documentName(name)
	                    .documentType(fileType)
	                    .uploadUrl(filePath)
	                    .serviceId(serviceId)
	                    .attachmentType(attachmentType)
	                    .createdAt(new Date())
	                    .createdBy(userId)
	                    .build();
	            documentRepository.save(documentSave);
	        }
	    }	
}
