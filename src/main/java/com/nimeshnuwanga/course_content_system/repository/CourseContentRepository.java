package com.nimeshnuwanga.course_content_system.repository;


import com.nimeshnuwanga.course_content_system.entity.CourseContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseContentRepository extends JpaRepository<CourseContent, Long> {
    List<CourseContent> findAllByOrderByUploadDateDesc();
}