package com.converterdocs.converter.controller;

import com.converterdocs.converter.service.DriveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

@RestController
public class ExportGoogleDocController {

    private DriveService driveService;

    @GetMapping(value = "/getFiles/{fileId}", produces="application/zip")
    public ResponseEntity<InputStreamResource> getFileFromGoogle(@PathVariable("fileId") String fileId){

        InputStream streamResponse = null;
        try {
            streamResponse = driveService.getStreamResponse(fileId);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + fileId + ".zip");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(new InputStreamResource(streamResponse));

    }

    @Autowired
    public void setDriveService(DriveService driveService) {
        this.driveService = driveService;
    }
}
