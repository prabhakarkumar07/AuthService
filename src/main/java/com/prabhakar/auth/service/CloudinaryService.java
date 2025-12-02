package com.prabhakar.auth.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadProfile(MultipartFile file) {
        try {
            Map upload = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "profile_images",
                            "resource_type", "image",
                            "transformation", new Object[]{ 
                                    ObjectUtils.asMap(
                                            "gravity", "face",
                                            "radius", "max",      // <-- makes it round
                                            "crop", "thumb",
                                            "width", 300,
                                            "height", 300
                                    )
                            }
                    )
            );

            return upload.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile image");
        }
    }


}
