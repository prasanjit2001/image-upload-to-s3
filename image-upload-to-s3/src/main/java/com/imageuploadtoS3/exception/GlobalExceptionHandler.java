package com.imageuploadtoS3.exception;

import com.imageuploadtoS3.response.CustomResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<CustomResponse> handleImageUpload(ImageUploadException imageUploadException) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CustomResponse.builder()
                        .message(imageUploadException.getMessage())
                        .success(false)
                        .build());
    }
}
