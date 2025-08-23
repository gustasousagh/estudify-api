package com.gustasousagh.studify.estudify.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private Long id;
    private String userId;
    private String courseTitle;
    private String courseDescription;
    private String courseImageUrl;
}
