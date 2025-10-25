package com.nimeshnuwanga.course_content_system.service;

import com.nimeshnuwanga.course_content_system.entity.CourseContent;
import com.nimeshnuwanga.course_content_system.exception.FileNotFoundException;
import com.nimeshnuwanga.course_content_system.exception.FileStorageException;
import com.nimeshnuwanga.course_content_system.repository.CourseContentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @TempDir
    Path tempDir;
    @Mock
    private CourseContentRepository courseContentRepository;
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString(), courseContentRepository);
    }

    @Test
    void storeFile_ValidPDF_Success() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "Test PDF content".getBytes()
        );

        CourseContent savedContent = new CourseContent();
        savedContent.setId(1L);
        savedContent.setFileName("test-document.pdf");
        savedContent.setFileType("application/pdf");
        savedContent.setFileSize(file.getSize());

        when(courseContentRepository.save(any(CourseContent.class))).thenReturn(savedContent);

        CourseContent result = fileStorageService.storeFile(file);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFileName()).isEqualTo("test-document.pdf");
        assertThat(result.getFileType()).isEqualTo("application/pdf");

        ArgumentCaptor<CourseContent> captor = ArgumentCaptor.forClass(CourseContent.class);
        verify(courseContentRepository).save(captor.capture());

        CourseContent capturedContent = captor.getValue();
        assertThat(capturedContent.getFileName()).isEqualTo("test-document.pdf");
        assertThat(capturedContent.getFileUrl()).endsWith(".pdf");
    }

    @Test
    void storeFile_ValidMP4_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "Test video content".getBytes()
        );

        CourseContent savedContent = new CourseContent();
        savedContent.setId(1L);
        savedContent.setFileName("test-video.mp4");
        savedContent.setFileType("video/mp4");

        when(courseContentRepository.save(any(CourseContent.class))).thenReturn(savedContent);

        CourseContent result = fileStorageService.storeFile(file);

        assertThat(result).isNotNull();
        assertThat(result.getFileName()).isEqualTo("test-video.mp4");
        verify(courseContentRepository, times(1)).save(any(CourseContent.class));
    }

    @Test
    void storeFile_ValidImage_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "Test image content".getBytes()
        );

        CourseContent savedContent = new CourseContent();
        savedContent.setId(1L);
        savedContent.setFileName("test-image.jpg");

        when(courseContentRepository.save(any(CourseContent.class))).thenReturn(savedContent);

        CourseContent result = fileStorageService.storeFile(file);

        assertThat(result).isNotNull();
        verify(courseContentRepository, times(1)).save(any(CourseContent.class));
    }

    @Test
    void storeFile_EmptyFile_ThrowsException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThatThrownBy(() -> fileStorageService.storeFile(emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Failed to store empty file");

        verify(courseContentRepository, never()).save(any());
    }

    @Test
    void storeFile_InvalidFileType_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.txt",
                "text/plain",
                "Test content".getBytes()
        );

        assertThatThrownBy(() -> fileStorageService.storeFile(file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid file type");

        verify(courseContentRepository, never()).save(any());
    }

    @Test
    void storeFile_FileTooLarge_ThrowsException() {
        byte[] largeContent = new byte[52428801]; // 50MB + 1 byte
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-file.pdf",
                "application/pdf",
                largeContent
        );

        assertThatThrownBy(() -> fileStorageService.storeFile(largeFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("File size exceeds maximum limit");

        verify(courseContentRepository, never()).save(any());
    }

    @Test
    void storeFile_FileNameWithInvalidPath_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../../../etc/passwd",
                "application/pdf",
                "Test content".getBytes()
        );

        assertThatThrownBy(() -> fileStorageService.storeFile(file))
                .isInstanceOf(FileStorageException.class)
                .hasMessageContaining("invalid path sequence");

        verify(courseContentRepository, never()).save(any());
    }

    @Test
    void loadFileAsResource_ExistingFile_Success() throws IOException {
        String fileName = "test-file.pdf";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, "Test content".getBytes());

        Resource resource = fileStorageService.loadFileAsResource(fileName);

        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
        assertThat(resource.getFilename()).isEqualTo(fileName);
    }

    @Test
    void loadFileAsResource_NonExistingFile_ThrowsException() {
        assertThatThrownBy(() -> fileStorageService.loadFileAsResource("nonexistent.pdf"))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("File not found");
    }

    @Test
    void getAllFiles_ReturnsAllFiles() {
        CourseContent content1 = new CourseContent();
        content1.setId(1L);
        content1.setFileName("file1.pdf");

        CourseContent content2 = new CourseContent();
        content2.setId(2L);
        content2.setFileName("file2.mp4");

        List<CourseContent> contentList = Arrays.asList(content1, content2);
        when(courseContentRepository.findAllByOrderByUploadDateDesc()).thenReturn(contentList);

        List<CourseContent> result = fileStorageService.getAllFiles();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFileName()).isEqualTo("file1.pdf");
        assertThat(result.get(1).getFileName()).isEqualTo("file2.mp4");
        verify(courseContentRepository, times(1)).findAllByOrderByUploadDateDesc();
    }

    @Test
    void getAllFiles_EmptyRepository_ReturnsEmptyList() {
        when(courseContentRepository.findAllByOrderByUploadDateDesc()).thenReturn(List.of());

        List<CourseContent> result = fileStorageService.getAllFiles();

        assertThat(result).isEmpty();
        verify(courseContentRepository, times(1)).findAllByOrderByUploadDateDesc();
    }

    @Test
    void getFileById_ExistingFile_Success() {
        CourseContent content = new CourseContent();
        content.setId(1L);
        content.setFileName("test-file.pdf");

        when(courseContentRepository.findById(1L)).thenReturn(Optional.of(content));

        CourseContent result = fileStorageService.getFileById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFileName()).isEqualTo("test-file.pdf");
        verify(courseContentRepository, times(1)).findById(1L);
    }

    @Test
    void getFileById_NonExistingFile_ThrowsException() {
        when(courseContentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileStorageService.getFileById(999L))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("File not found with id 999");

        verify(courseContentRepository, times(1)).findById(999L);
    }

    @Test
    void deleteFile_ExistingFile_Success() throws IOException {
        String fileName = "test-delete.pdf";
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, "Test content".getBytes());

        CourseContent content = new CourseContent();
        content.setId(1L);
        content.setFileName("test-delete.pdf");
        content.setFileUrl(fileName);

        when(courseContentRepository.findById(1L)).thenReturn(Optional.of(content));
        doNothing().when(courseContentRepository).delete(content);

        fileStorageService.deleteFile(1L);

        assertThat(Files.exists(filePath)).isFalse();
        verify(courseContentRepository, times(1)).findById(1L);
        verify(courseContentRepository, times(1)).delete(content);
    }

    @Test
    void deleteFile_NonExistingFile_ThrowsException() {
        when(courseContentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileStorageService.deleteFile(999L))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessageContaining("File not found with id 999");

        verify(courseContentRepository, times(1)).findById(999L);
        verify(courseContentRepository, never()).delete(any());
    }

    @Test
    void storeFile_AllAllowedExtensions_Success() {
        String[] fileNames = {"test.pdf", "test.mp4", "test.jpg", "test.jpeg", "test.png"};
        String[] mimeTypes = {"application/pdf", "video/mp4", "image/jpeg", "image/jpeg", "image/png"};

        for (int i = 0; i < fileNames.length; i++) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    fileNames[i],
                    mimeTypes[i],
                    "Test content".getBytes()
            );

            CourseContent savedContent = new CourseContent();
            savedContent.setId((long) (i + 1));
            savedContent.setFileName(fileNames[i]);

            when(courseContentRepository.save(any(CourseContent.class))).thenReturn(savedContent);

            CourseContent result = fileStorageService.storeFile(file);

            assertThat(result).isNotNull();
            assertThat(result.getFileName()).isEqualTo(fileNames[i]);
        }

        verify(courseContentRepository, times(fileNames.length)).save(any(CourseContent.class));
    }
}