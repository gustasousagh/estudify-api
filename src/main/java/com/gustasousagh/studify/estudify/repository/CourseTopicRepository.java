package com.gustasousagh.studify.estudify.repository;

import com.gustasousagh.studify.estudify.entity.CourseTopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseTopicRepository extends JpaRepository<CourseTopicEntity, Long> {

    List<CourseTopicEntity> findByCourseIdOrderByOrderAsc(Long courseId);

    Optional<CourseTopicEntity> findByCourseIdAndId(Long courseId, Long id);
}
