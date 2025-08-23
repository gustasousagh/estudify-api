package com.gustasousagh.studify.estudify.dto;

import com.gustasousagh.studify.estudify.enums.CourseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String userId;
    private String courseTitle;
    private String courseDescription;
    private String courseImageUrl;
    private boolean complete;
    private CourseType type;

    private Long courseTotalModules;
    private Long courseCompletedModules;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

