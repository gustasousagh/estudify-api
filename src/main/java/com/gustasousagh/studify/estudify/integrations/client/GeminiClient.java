package com.gustasousagh.studify.estudify.integrations.client;

import com.gustasousagh.studify.estudify.model.request.GenerateCourseRequest;
import reactor.core.publisher.Mono;

public interface GeminiClient {
    Mono<String> generateCourseRoadmap(GenerateCourseRequest generateCourseRequest);
    Mono<String> generateCourseContent(String courseContent, String courseTitle);
}
