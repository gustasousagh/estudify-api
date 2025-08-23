package com.gustasousagh.studify.estudify.controllers.user;

import com.gustasousagh.studify.estudify.dto.CourseDTO;
import com.gustasousagh.studify.estudify.dto.CourseSummaryDTO;
import com.gustasousagh.studify.estudify.model.request.GenerateCourseRequest;
import com.gustasousagh.studify.estudify.services.UserCourseService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
public class UserCourseController {

    private final UserCourseService courseService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<Page<CourseDTO>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        String userId = "1L";
        Page<CourseDTO> courses = courseService.getCoursesByUserId(userId, pageable);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/summary")
    public CourseSummaryDTO summary() {
        String userId = "1L";
        return courseService.getCourseSummary(userId);
    }

    @PostMapping
    public Mono<ResponseEntity<CourseDTO>> generate(@RequestBody GenerateCourseRequest req) {
        String userId = "1L";
        return courseService.generateAndSaveCourse(req, userId)
                .map(ResponseEntity::ok);
    }
}
