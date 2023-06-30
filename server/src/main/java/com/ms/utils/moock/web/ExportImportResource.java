package com.ms.utils.moock.web;

import com.ms.utils.moock.aop.exception.MoockApplicationException;
import com.ms.utils.moock.aop.interceptor.LogExecutionTime;
import com.ms.utils.moock.dto.MockResponse;
import com.ms.utils.moock.service.ExportImportService;
import com.ms.utils.moock.service.MockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/native/api/mocks/")
public class ExportImportResource {

    private final Logger LOGGER = LoggerFactory.getLogger(ExportImportResource.class);

    @Autowired
    private MockService mockService;

    @Autowired
    private ExportImportService exportImportService;

    @LogExecutionTime
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportMocks() {
        try {
            byte[] exportedData = exportImportService.exportMocks();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("mocks.json")
                    .build());
            return new ResponseEntity<>(exportedData, headers, HttpStatus.OK);
        } catch (IOException e) {
            // Handle the exception appropriately
            LOGGER.error("Error while exporting mocks : ", e);
            return new ResponseEntity(createResponse("Some error occurred while exporting mocks. Please try after sometime."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @LogExecutionTime
    @PostMapping("/import")
    public ResponseEntity<String> importMocks(@RequestParam("file") MultipartFile file) throws MoockApplicationException, IOException {
        exportImportService.importMocks(file);
        return new ResponseEntity(createResponse("Mocks imported successfully"), HttpStatus.OK);
    }

    private MockResponse createResponse(String message, Object data){
        return new MockResponse(message, data);
    }

    private MockResponse createResponse(Object data){
        return createResponse("", data);
    }
}
