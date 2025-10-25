package com.nimeshnuwanga.course_content_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimeshnuwanga.course_content_system.entity.CourseContent;
import com.nimeshnuwanga.course_content_system.exception.FileNotFoundException;
import com.nimeshnuwanga.course_content_system.exception.FileStorageException;
import com.nimeshnuwanga.course_content_system.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Autowired
    private ObjectMapper objectMapper;

    private CourseContent courseContent;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        courseContent = new CourseContent();
        courseContent.setId(1L);
        courseContent.setFileName("test-document.pdf");
        courseContent.setFileType("application/pdf");
        courseContent.setFileSize(1024000L);
        courseContent.setUploadDate(LocalDateTime.now());
        courseContent.setFileUrl("uuid-test-document.pdf");

        mockFile = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "Test PDF content".getBytes()
        );
    }

    @Test
    void uploadFile_Success() throws Exception {

        when(fileStorageService.storeFile(any())).thenReturn(courseContent);

        mockMvc.perform(multipart("/api/files/upload")
                        .file(mockFile))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("test-document.pdf"))
                .andExpect(jsonPath("$.fileType").value("application/pdf"))
                .andExpect(jsonPath("$.fileSize").value(1024000))
                .andExpect(jsonPath("$.message").value("File uploaded successfully"))
                .andExpect(jsonPath("$.fileUrl").value(containsString("/api/files/download/")));

        verify(fileStorageService, times(1)).storeFile(any());
    }

    @Test
    void uploadFile_InvalidFileType() throws Exception {

        when(fileStorageService.storeFile(any()))
                .thenThrow(new IllegalArgumentException("Invalid file type. Only PDF, MP4, JPG, JPEG, and PNG files are allowed"));

        mockMvc.perform(multipart("/api/files/upload")
                        .file(mockFile))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid Request"))
                .andExpect(jsonPath("$.details").value(containsString("Invalid file type")));

        verify(fileStorageService, times(1)).storeFile(any());
    }

    @Test
    void uploadFile_FileTooLarge() throws Exception {

        when(fileStorageService.storeFile(any()))
                .thenThrow(new IllegalArgumentException("File size exceeds maximum limit of 50MB"));

        mockMvc.perform(multipart("/api/files/upload")
                        .file(mockFile))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid Request"))
                .andExpect(jsonPath("$.details").value(containsString("File size exceeds")));

        verify(fileStorageService, times(1)).storeFile(any());
    }

    @Test
    void uploadFile_EmptyFile() throws Exception {

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );
        when(fileStorageService.storeFile(any()))
                .thenThrow(new IllegalArgumentException("Failed to store empty file"));

        mockMvc.perform(multipart("/api/files/upload")
                        .file(emptyFile))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid Request"));

        verify(fileStorageService, times(1)).storeFile(any());
    }

    @Test
    void uploadFile_StorageException() throws Exception {

        when(fileStorageService.storeFile(any()))
                .thenThrow(new FileStorageException("Could not store file. Please try again!"));

        mockMvc.perform(multipart("/api/files/upload")
                        .file(mockFile))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("File Storage Error"))
                .andExpect(jsonPath("$.details").value(containsString("Could not store file")));

        verify(fileStorageService, times(1)).storeFile(any());
    }

    @Test
    void getAllFiles_Success() throws Exception {

        CourseContent courseContent2 = new CourseContent();
        courseContent2.setId(2L);
        courseContent2.setFileName("test-video.mp4");
        courseContent2.setFileType("video/mp4");
        courseContent2.setFileSize(5120000L);
        courseContent2.setUploadDate(LocalDateTime.now());
        courseContent2.setFileUrl("uuid-test-video.mp4");

        List<CourseContent> fileList = Arrays.asList(courseContent, courseContent2);
        when(fileStorageService.getAllFiles()).thenReturn(fileList);


        mockMvc.perform(get("/api/files/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fileName").value("test-document.pdf"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].fileName").value("test-video.mp4"));

        verify(fileStorageService, times(1)).getAllFiles();
    }

    @Test
    void getAllFiles_EmptyList() throws Exception {

        when(fileStorageService.getAllFiles()).thenReturn(List.of());


        mockMvc.perform(get("/api/files/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(fileStorageService, times(1)).getAllFiles();
    }

    @Test
    void getFileById_Success() throws Exception {

        when(fileStorageService.getFileById(1L)).thenReturn(courseContent);


        mockMvc.perform(get("/api/files/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fileName").value("test-document.pdf"))
                .andExpect(jsonPath("$.fileType").value("application/pdf"))
                .andExpect(jsonPath("$.fileSize").value(1024000));

        verify(fileStorageService, times(1)).getFileById(1L);
    }

    @Test
    void getFileById_NotFound() throws Exception {

        when(fileStorageService.getFileById(999L))
                .thenThrow(new FileNotFoundException("File not found with id 999"));


        mockMvc.perform(get("/api/files/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("File Not Found"))
                .andExpect(jsonPath("$.details").value(containsString("File not found with id 999")));

        verify(fileStorageService, times(1)).getFileById(999L);
    }

    @Test
    void downloadFile_Success() throws Exception {

        byte[] fileContent = "Test PDF content".getBytes();
        Resource resource = new ByteArrayResource(fileContent);
        when(fileStorageService.loadFileAsResource(anyString())).thenReturn(resource);


        mockMvc.perform(get("/api/files/download/uuid-test-document.pdf"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));

        verify(fileStorageService, times(1)).loadFileAsResource("uuid-test-document.pdf");
    }

    @Test
    void downloadFile_NotFound() throws Exception {

        when(fileStorageService.loadFileAsResource(anyString()))
                .thenThrow(new FileNotFoundException("File not found"));

        mockMvc.perform(get("/api/files/download/nonexistent.pdf"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("File Not Found"));

        verify(fileStorageService, times(1)).loadFileAsResource("nonexistent.pdf");
    }

    @Test
    void deleteFile_Success() throws Exception {

        doNothing().when(fileStorageService).deleteFile(1L);

        mockMvc.perform(delete("/api/files/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("File deleted successfully"));

        verify(fileStorageService, times(1)).deleteFile(1L);
    }

    @Test
    void deleteFile_NotFound() throws Exception {

        doThrow(new FileNotFoundException("File not found with id 999"))
                .when(fileStorageService).deleteFile(999L);

        mockMvc.perform(delete("/api/files/999"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("File Not Found"))
                .andExpect(jsonPath("$.details").value(containsString("File not found with id 999")));

        verify(fileStorageService, times(1)).deleteFile(999L);
    }

    @Test
    void deleteFile_StorageException() throws Exception {

        doThrow(new FileStorageException("Could not delete file"))
                .when(fileStorageService).deleteFile(1L);

        mockMvc.perform(delete("/api/files/1"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("File Storage Error"))
                .andExpect(jsonPath("$.details").value(containsString("Could not delete file")));

        verify(fileStorageService, times(1)).deleteFile(1L);
    }
}