package com.gustasousagh.studify.estudify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseSummaryDTO {
    private long totalCourses;
    private long totalQuizzes;
    private long completedCourses;
    private long incompleteCourses;
}
