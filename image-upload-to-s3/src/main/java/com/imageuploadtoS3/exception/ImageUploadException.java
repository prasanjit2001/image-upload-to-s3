package com.imageuploadtoS3.exception;

import lombok.Builder;
import lombok.Data;

@Data
public class ImageUploadException extends RuntimeException {
public ImageUploadException(String message){
super(message);
}



}
