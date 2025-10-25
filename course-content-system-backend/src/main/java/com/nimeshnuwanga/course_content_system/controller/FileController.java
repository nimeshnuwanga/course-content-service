package com.nimeshnuwanga.course_content_system.controller;

import com.nimeshnuwanga.course_content_system.dto.FileUploadResponse;
import com.nimeshnuwanga.course_content_system.entity.CourseContent;
import com.nimeshnuwanga.course_content_system.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        CourseContent courseContent = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/download/")
                .path(courseContent.getFileUrl())
                .toUriString();

        FileUploadResponse response = new FileUploadResponse(
                courseContent.getId(),
                courseContent.getFileName(),
                courseContent.getFileType(),
                courseContent.getFileSize(),
                courseContent.getUploadDate(),
                fileDownloadUri,
                "File uploaded successfully"
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CourseContent>> getAllFiles() {
        List<CourseContent> files = fileStorageService.getAllFiles();

        files.forEach(file -> {
            String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/files/download/")
                    .path(file.getFileUrl())
                    .toUriString();
            file.setFileUrl(fileDownloadUri);
        });

        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseContent> getFileById(@PathVariable Long id) {
        CourseContent file = fileStorageService.getFileById(id);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/download/")
                .path(file.getFileUrl())
                .toUriString();
        file.setFileUrl(fileDownloadUri);

        return ResponseEntity.ok(file);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Could not determine file type
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        fileStorageService.deleteFile(id);
        return ResponseEntity.ok("File deleted successfully");
    }
}