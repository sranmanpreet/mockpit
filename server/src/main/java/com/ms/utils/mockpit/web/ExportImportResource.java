package com.ms.utils.mockpit.web;

import com.ms.utils.mockpit.aop.exception.MockpitApplicationException;
import com.ms.utils.mockpit.aop.interceptor.LogExecutionTime;
import com.ms.utils.mockpit.dto.MockResponse;
import com.ms.utils.mockpit.service.ExportImportService;
import com.ms.utils.mockpit.service.MockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("mocks.json")
                    .build());
            headers.setAccessControlExposeHeaders(Arrays.asList(HttpHeaders.CONTENT_DISPOSITION));
            return new ResponseEntity<>(exportedData, headers, HttpStatus.OK);
        } catch (IOException e) {
            // Handle the exception appropriately
            LOGGER.error("Error while exporting mocks : ", e);
            return new ResponseEntity(createResponse("Some error occurred while exporting mocks. Please try after sometime."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @LogExecutionTime
    @PostMapping("/import")
    public ResponseEntity<String> importMocks(@RequestParam("file") MultipartFile file) throws MockpitApplicationException, IOException {
        exportImportService.importMocks(file);
        return new ResponseEntity(createResponse("Mocks imported successfully", null), HttpStatus.OK);
    }

    private MockResponse createResponse(String message, Object data){
        return new MockResponse(message, data);
    }

    private MockResponse createResponse(Object data){
        return createResponse("", data);
    }
}
