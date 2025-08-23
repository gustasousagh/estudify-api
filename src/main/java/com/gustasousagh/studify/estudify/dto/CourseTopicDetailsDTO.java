package com.gustasousagh.studify.estudify.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseTopicDetailsDTO {
    private Long id;
    private String topicTitle;
    private String topicDescription;
    private String topicContent;
    private Boolean complete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
