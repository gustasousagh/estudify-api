package com.gustasousagh.studify.estudify.controllers.user;

import com.gustasousagh.studify.estudify.dto.CourseTopicDTO;
import com.gustasousagh.studify.estudify.dto.CourseTopicDetailsDTO;
import com.gustasousagh.studify.estudify.services.UserCourseTopicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-topics")
@RequiredArgsConstructor
public class UserCourseTopicController {

    private final UserCourseTopicService courseTopicService;

    @GetMapping("/course/{courseId}")
    public List<CourseTopicDTO> getAllTopicsByCourseId(@PathVariable Long courseId) {
        return courseTopicService.getAllTopicTitlesByCourseId(courseId);
    }

    @GetMapping("/course/{courseId}/topic/{topicId}")
    public CourseTopicDetailsDTO getTopicDetails(@PathVariable Long courseId, @PathVariable Long topicId) {
        return courseTopicService.getTopicDetails(courseId, topicId);
    }

    @PatchMapping("/course/{courseId}/topic/{topicId}/complete")
    public ResponseEntity<Void> completeTopic(@PathVariable Long courseId, @PathVariable Long topicId) {
        Long userId = 1L;
        courseTopicService.completeTopic(courseId, topicId, userId);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }
}
