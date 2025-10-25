package com.nimeshnuwanga.course_content_system.service;

import com.nimeshnuwanga.course_content_system.entity.CourseContent;
import com.nimeshnuwanga.course_content_system.exception.FileNotFoundException;
import com.nimeshnuwanga.course_content_system.exception.FileStorageException;
import com.nimeshnuwanga.course_content_system.repository.CourseContentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "mp4", "jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE = 52428800;
    private final Path fileStorageLocation;
    private final CourseContentRepository courseContentRepository;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir,
                              CourseContentRepository courseContentRepository) {
        this.courseContentRepository = courseContentRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public CourseContent storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);

        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // Generate unique file name
            String fileExtension = getFileExtension(originalFileName);
            String newFileName = UUID.randomUUID() + "." + fileExtension;

            // Copy file to the target location
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Save metadata to database
            CourseContent courseContent = new CourseContent();
            courseContent.setFileName(originalFileName);
            courseContent.setFileType(file.getContentType());
            courseContent.setFileSize(file.getSize());
            courseContent.setFileUrl(newFileName);

            return courseContentRepository.save(courseContent);

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } catch (Exception ex) {
            throw new FileNotFoundException("File not found " + fileName, ex);
        }
    }

    public List<CourseContent> getAllFiles() {
        return courseContentRepository.findAllByOrderByUploadDateDesc();
    }

    public CourseContent getFileById(Long id) {
        return courseContentRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found with id " + id));
    }

    public void deleteFile(Long id) {
        CourseContent courseContent = getFileById(id);

        try {
            // Delete physical file
            Path filePath = this.fileStorageLocation.resolve(courseContent.getFileUrl()).normalize();
            Files.deleteIfExists(filePath);

            // Delete database record
            courseContentRepository.delete(courseContent);

        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + courseContent.getFileName(), ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 50MB");
        }

        // Check file extension
        String fileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(fileName);

        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only PDF, MP4, JPG, JPEG, and PNG files are allowed");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}