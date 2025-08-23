package com.gustasousagh.studify.estudify.services;

import com.gustasousagh.studify.estudify.dto.CourseDTO;
import com.gustasousagh.studify.estudify.dto.CourseSummaryDTO;
import com.gustasousagh.studify.estudify.model.request.GenerateCourseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface UserCourseService {
    Page<CourseDTO> getCoursesByUserId(String userId, Pageable pageable);
    Mono<CourseDTO> generateAndSaveCourse(GenerateCourseRequest req, String userId);
    CourseSummaryDTO getCourseSummary(String userId);
}
