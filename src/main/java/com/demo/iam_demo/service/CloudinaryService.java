package com.demo.iam_demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.demo.iam_demo.exception.AppException;
import com.demo.iam_demo.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file, String folderName){
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                        "folder", folderName,
                        "resource_type", "image"
                    )
            );

            Object url = uploadResult.get("secure_url");
            if(url == null){
                throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED, "No secure_url return from Cloudinary");
            }
            return url.toString(); // url ảnh mạng
        } catch (IOException e){
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED, "Upload image failed: " + e.getMessage());
        } catch (Exception e){
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAILED, "Unexpected error: " + e.getMessage());
        }
    }
}
