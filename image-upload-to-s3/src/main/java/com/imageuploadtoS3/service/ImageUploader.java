package com.imageuploadtoS3.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


public interface ImageUploader {
    String uploadImage(MultipartFile image);
    List<Map<String, String>> allFiles();
    String preSignedUrl(String fileName);

    String getImageByUrlName(String filename);
}
