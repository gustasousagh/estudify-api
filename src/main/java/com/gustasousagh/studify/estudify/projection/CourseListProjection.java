package com.gustasousagh.studify.estudify.projection;

import com.gustasousagh.studify.estudify.enums.CourseStatus;
import com.gustasousagh.studify.estudify.enums.CourseType;

import java.time.LocalDateTime;

public interface CourseListProjection {
    Long getId();
    String getUserId();
    String getCourseTitle();
    String getCourseDescription();
    String getCourseImageUrl();
    boolean isComplete();
    CourseType getType();

    long getTotalModules();
    long getCompletedModules();

    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
}
