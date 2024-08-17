package com.imageuploadtoS3.controller;

import com.imageuploadtoS3.service.ImageUploader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/s3")
public class S3Controller {
    @Autowired
    private ImageUploader uploader;

    @PostMapping("/upload")
    public ResponseEntity<?>uploadImage( @RequestParam MultipartFile file){
        String url = uploader.uploadImage(file);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/files")
    public ResponseEntity<?> listAllFiles() {
        return ResponseEntity.ok(uploader.allFiles());
    }
    @GetMapping("/file/{filename}")
    public ResponseEntity<?> getFileUrl(@PathVariable String filename) {
            String url = uploader.getImageByUrlName(filename);
            return ResponseEntity.ok(url);
    }



}
