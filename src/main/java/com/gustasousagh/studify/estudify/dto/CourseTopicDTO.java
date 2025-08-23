package com.gustasousagh.studify.estudify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseTopicDTO {
    private Long id;
    private String topicTitle;
    private Boolean complete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
