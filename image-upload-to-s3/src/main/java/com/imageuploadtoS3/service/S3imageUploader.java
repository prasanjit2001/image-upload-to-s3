package com.imageuploadtoS3.service;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import com.imageuploadtoS3.exception.ImageUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class S3imageUploader implements ImageUploader {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private S3Presigner s3Presigner;

    @Value("${cloud.s3.bucket}")
    private String bucketName;

    @Override
    public String uploadImage(MultipartFile image) {
        if (image == null) {
            throw new ImageUploadException("Image is null");
        }

        String actualFileName = image.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + actualFileName.substring(actualFileName.lastIndexOf("."));

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(image.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(image.getInputStream(), image.getSize()));


            return preSignedUrl(fileName);  // Use the preSignedUrl method to get the URL
        } catch (IOException e) {
            throw new ImageUploadException("Error in uploading image: " + e.getMessage());
        }
    }


    @Override
    public List<Map<String, String>> allFiles() {
        try {
            // Create a request to list objects in the bucket
            ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .build();

            // Get the list of objects
            ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);

            // Extract the keys from the object summaries
            List<Map<String, String>> fileInfoList = listObjectsV2Response.contents().stream()
                    .map(s3Object -> {
                        String fileName = s3Object.key();
                        String presignedUrl = preSignedUrl(fileName);
                        return Map.of("fileName", fileName, "url", presignedUrl);
                    })
                    .collect(Collectors.toList());

            // Return the list of file names and URLs
            return fileInfoList;
        } catch (S3Exception e) {
            log.error("Error listing files: {}", e.getMessage(), e);
            throw new ImageUploadException("Error listing files: " + e.getMessage());
        }
    }

    @Override
    public String preSignedUrl(String fileName) {
        try {
            // Create a request to get the object
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            // Create a presigned URL request
            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
                    builder -> builder.getObjectRequest(getObjectRequest)
                            .signatureDuration(Duration.ofMinutes(10)) // Set URL expiration duration
                            .build()
            );

            // Log the generated URL for debugging
            String url = presignedGetObjectRequest.url().toString();
            log.info("Generated pre-signed URL: {}", url);

            // Return the pre-signed URL as a string
            return url;
        } catch (S3Exception e) {
            log.error("Error generating pre-signed URL: {}", e.getMessage(), e);
            throw new ImageUploadException("Error generating pre-signed URL: " + e.getMessage());
        }
    }

    @Override
    public String getImageByUrlName(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new ImageUploadException("Filename is null or empty");
        }

        try {
            // Generate a pre-signed URL for the specified filename
            return preSignedUrl(filename);
        } catch (Exception e) {
            log.error("Error retrieving image by URL name: {}", e.getMessage(), e);
            throw new ImageUploadException("Error retrieving image by URL name: " + e.getMessage());
        }
    }

}
