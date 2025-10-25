package com.nimeshnuwanga.course_content_system.repository;

import com.nimeshnuwanga.course_content_system.entity.CourseContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CourseContentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CourseContentRepository courseContentRepository;

    private CourseContent courseContent1;
    private CourseContent courseContent2;
    private CourseContent courseContent3;

    @BeforeEach
    void setUp() {
        courseContent1 = new CourseContent();
        courseContent1.setFileName("document1.pdf");
        courseContent1.setFileType("application/pdf");
        courseContent1.setFileSize(1024000L);
        courseContent1.setUploadDate(LocalDateTime.now().minusDays(3));
        courseContent1.setFileUrl("uuid-document1.pdf");

        courseContent2 = new CourseContent();
        courseContent2.setFileName("video.mp4");
        courseContent2.setFileType("video/mp4");
        courseContent2.setFileSize(5120000L);
        courseContent2.setUploadDate(LocalDateTime.now().minusDays(1));
        courseContent2.setFileUrl("uuid-video.mp4");

        courseContent3 = new CourseContent();
        courseContent3.setFileName("image.jpg");
        courseContent3.setFileType("image/jpeg");
        courseContent3.setFileSize(512000L);
        courseContent3.setUploadDate(LocalDateTime.now());
        courseContent3.setFileUrl("uuid-image.jpg");
    }

    @Test
    void save_ValidCourseContent_Success() {
        CourseContent saved = courseContentRepository.save(courseContent1);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getFileName()).isEqualTo("document1.pdf");
        assertThat(saved.getFileType()).isEqualTo("application/pdf");
        assertThat(saved.getFileSize()).isEqualTo(1024000L);
        assertThat(saved.getUploadDate()).isNotNull();
    }

    @Test
    void findById_ExistingContent_Success() {
        CourseContent saved = entityManager.persistAndFlush(courseContent1);

        Optional<CourseContent> found = courseContentRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getFileName()).isEqualTo("document1.pdf");
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void findById_NonExistingContent_ReturnsEmpty() {
        Optional<CourseContent> found = courseContentRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAll_ReturnsAllContent() {

        entityManager.persist(courseContent1);
        entityManager.persist(courseContent2);
        entityManager.persist(courseContent3);
        entityManager.flush();

        List<CourseContent> allContent = courseContentRepository.findAll();

        assertThat(allContent).hasSize(3);
        assertThat(allContent).extracting(CourseContent::getFileName)
                .containsExactlyInAnyOrder("document1.pdf", "video.mp4", "image.jpg");
    }

    @Test
    void findAllByOrderByUploadDateDesc_ReturnsOrderedContent() {
        entityManager.persist(courseContent1);
        entityManager.persist(courseContent2);
        entityManager.persist(courseContent3);
        entityManager.flush();

        List<CourseContent> orderedContent = courseContentRepository.findAllByOrderByUploadDateDesc();

        assertThat(orderedContent).hasSize(3);
        assertThat(orderedContent.get(0).getFileName()).isEqualTo("image.jpg"); // Most recent
        assertThat(orderedContent.get(1).getFileName()).isEqualTo("video.mp4");
        assertThat(orderedContent.get(2).getFileName()).isEqualTo("document1.pdf"); // Oldest
    }

    @Test
    void delete_ExistingContent_Success() {

        CourseContent saved = entityManager.persistAndFlush(courseContent1);
        Long id = saved.getId();

        courseContentRepository.delete(saved);
        entityManager.flush();

        Optional<CourseContent> deleted = courseContentRepository.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    void deleteById_ExistingContent_Success() {

        CourseContent saved = entityManager.persistAndFlush(courseContent1);
        Long id = saved.getId();

        courseContentRepository.deleteById(id);
        entityManager.flush();

        Optional<CourseContent> deleted = courseContentRepository.findById(id);
        assertThat(deleted).isEmpty();
    }

    @Test
    void count_ReturnsCorrectCount() {
        entityManager.persist(courseContent1);
        entityManager.persist(courseContent2);
        entityManager.flush();

        long count = courseContentRepository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void existsById_ExistingContent_ReturnsTrue() {
        CourseContent saved = entityManager.persistAndFlush(courseContent1);

        boolean exists = courseContentRepository.existsById(saved.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsById_NonExistingContent_ReturnsFalse() {
        boolean exists = courseContentRepository.existsById(999L);

        assertThat(exists).isFalse();
    }

    @Test
    void update_ExistingContent_Success() {
        CourseContent saved = entityManager.persistAndFlush(courseContent1);
        entityManager.clear();

        saved.setFileName("updated-document.pdf");
        CourseContent updated = courseContentRepository.save(saved);
        entityManager.flush();

        CourseContent found = entityManager.find(CourseContent.class, saved.getId());
        assertThat(found.getFileName()).isEqualTo("updated-document.pdf");
    }

    @Test
    void deleteAll_RemovesAllContent() {
        entityManager.persist(courseContent1);
        entityManager.persist(courseContent2);
        entityManager.flush();

        courseContentRepository.deleteAll();
        entityManager.flush();

        List<CourseContent> allContent = courseContentRepository.findAll();
        assertThat(allContent).isEmpty();
    }
}