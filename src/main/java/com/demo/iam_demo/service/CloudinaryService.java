package com.demo.iam_demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
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
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", "image"
            ));
            return uploadResult.get("secure_url").toString(); // url ảnh mạng
        } catch (IOException e){
            throw new RuntimeException("Upload image failed", e);
        }
    }
}
